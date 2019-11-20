#!/bin/bash

ROOT_PATH="$HOME/minerva"
MISC_DIR="${ROOT_PATH}/.misc"
PID_FILE="${MISC_DIR}/pid.file"
DB_FILE="$ROOT_PATH/minerva.db"
DATA_PATH="$ROOT_PATH/data"
PARALLELISM=4

function guard_runs {
    if [ ! -f "$PID_FILE" ]
    then
        echo $$ > "$PID_FILE"
    else
        if ps -p $(cat "$PID_FILE") > /dev/null 2>&1
        then
            exit 0
        else
            echo $$ > "$PID_FILE"
        fi
    fi
}

function await {
    local pids=( "$@" )

    for pid in "${pids[@]}";
    do
        wait $pid
    done
}

function prepare_db {
    sqlite3 $DB_FILE <<EOF
create table if not exists apps (id TEXT PRIMARY KEY);
create table if not exists running (id TEXT PRIMARY KEY);
create table if not exists done (id TEXT PRIMARY KEY);
create table if not exists retrieved (id TEXT PRIMARY KEY);
create table if not exists results (id TEXT PRIMARY KEY);
create table if not exists failed (id TEXT, reason TEXT);
EOF
}

function process_running_apps {
    local running=$(sqlite3 "$DB_FILE" "SELECT * FROM running;")
    local pids=()
    while IFS= read -r app;
    do
        process_app "$app" &
        pids+=($!)
    done < <(printf '%s\n' "$running" | grep -v '^$')

    await "${pids[@]}"
}

function process_app {
    local app_status="$(yarn_status $1)"
    local state=$(echo "$app_status" | app_state)
    local resolution=$(echo "$app_status" | app_resolution)

    if [ "$state" = 'FINISHED' ] && [ "$resolution" = 'SUCCEEDED' ]
    then
        move_app running "done" $1
    elif [ "$STATE" = 'FAILED' ] || [ "$STATE" = 'KILLED' ] || [ "$RESOLUTION" = 'FAILED' ] || [ "$RESOLUTION" = 'KILLED' ]
    then
        move_to_failed running $1 "FATAL: Application ended with state $state and resolution $resolution!"
    fi
}

function process_done_apps {
    local done_apps=$(sqlite3 "$DB_FILE" "SELECT * FROM done;")
    local pids=()
    while IFS= read -r app;
    do
        fetch_app_data "$app" &
        pids+=($!)
    done < <(printf '%s\n' "$done_apps" | grep -v '^$')

    await "${pids[@]}"
}

function yarn_status {
    yarn application -status $1 2>/dev/null
}

function app_state {
    grep "[[:space:]]State[[:space:]]:[[:space:]].*$" | awk 'NF{ print $NF }'
}

function app_resolution {
    grep "[[:space:]]Final-State[[:space:]]:[[:space:]].*$" | awk 'NF{ print $NF }'
}

function log_aggregation_state {
    grep "[[:space:]]Log[[:space:]]Aggregation[[:space:]]Status[[:space:]]:[[:space:]].*$" | awk 'NF{ print $NF }'
}

function move_app {
    local exists=$(sqlite3 $DB_FILE "SELECT EXISTS(SELECT 1 FROM $1 WHERE id='$3');")

    if [ "$exists" = "1" ]
    then
        sqlite3 $DB_FILE <<EOF
        BEGIN;

        INSERT INTO $2
        SELECT * FROM $1
        WHERE id='$3';

        DELETE FROM $1
        WHERE id='$3';

        COMMIT;
EOF
    else
        sqlite3 $DB_FILE <<EOF
        INSERT INTO failed
        SELECT '$3' as id, 'Move from $1 to $2. Application didn't exist in $1!' as reason;
EOF
    fi
}

function move_to_failed {
    local exists=$(sqlite3 $DB_FILE "SELECT EXISTS(SELECT 1 FROM $1 WHERE id='$2');")

    if [ "$exists" = "1" ]
    then
        sqlite3 $DB_FILE <<EOF
        BEGIN;

        INSERT INTO failed
        SELECT '$2' as id, '$3' as reason;

        DELETE FROM $1
        WHERE id='$2';

        COMMIT;
EOF
    else
        sqlite3 $DB_FILE <<EOF
        INSERT INTO failed
        SELECT '$2' as id, 'Application didn't exist in $1 table!' as reason;
EOF
    fi
}

function start_apps {
    local running=$(sqlite3 $DB_FILE "SELECT COUNT(*) FROM running;")
    local startable=$((PARALLELISM-running))

    if [ "$startable" -gt "0" ]
    then
        echo "starting $startable apps!"
    fi
}

function fetch_app_data {
    local log_status=$(yarn_status $1 | log_aggregation_state)

    if [ "$log_status" = 'SUCCEEDED' ] || [ "$log_status" = 'TIME_OUT' ] || [ "$log_status" = 'N/A' ]
    then
            mkdir -p "$DATA_PATH/$1"

            local history_status=0
            local log_status=0

            local history_pid=$(retrieve_history "$1" &)
            local log_pid=$(retrieve_logs "$1" &)

            wait $history_pid || history_status=1
            wait $log_pid || log_status=1

            if [ "$history_status" = "0" ] && [ "$log_status" = "0" ]
            then
                move_app "done" retrieved $1
            else
                rm -rf "$DATA_PATH/$1"
                move_to_failed "done" $1 "Failed to retrieve log or history. Exit-Codes: Log $log_status, History $history_status"
            fi
    elif [ "$log_status" = 'FAILED' ] || [ "$log_status" = 'DISABLED' ]
    then
            move_to_failed "done" $1 "FATAL: log aggregation has/is $log_status"
    fi
}

function retrieve_history {
    local hdfs_location=$(hdfs dfs -ls /spark2-history | grep $1 | awk 'NF{ print $NF }' | head -n 1)

    if [ -n "$hdfs_location" ]
    then
            hdfs dfs -copyToLocal "$hdfs_location" "$DATA_PATH/$1/history"
            return 0
    else
            return 1
    fi
}

function retrieve_logs {
    local log_file="$DATA_PATH/$1/logs"

    yarn logs -applicationId $1 2>/dev/null > "$log_file"

    if [ ! -f "$log_file" ] || [ $(wc -l < "$log_file") -lt "1" ]
    then
            return 1
    fi

    return 0
}

#######################################################
######################  MAIN  #########################
#######################################################

# make sure all necessary directories exist
mkdir -p $MISC_DIR
mkdir -p $DATA_PATH

# make sure only one instance can run simultaneously
guard_runs

# create db and/or tables if necessary
prepare_db

process_running_apps
start_apps
process_done_apps
