package de.flostadler.minerva.cmd

import java.util.concurrent.{Executors, TimeUnit}

import jline.Terminal.getTerminal


object EnhancedProgressBar {
  def apply(count: Long, printer: String => Unit = s => print(s"\r$s")): EnhancedProgressBar =
    new EnhancedProgressBar(count, printer)
}

class EnhancedProgressBar(val total: Long, val printer: String => Unit) extends AutoCloseable {
  private val monitor = new Object()

  private var count = 0L
  private var elapsedTime = 0L

  private val executor = Executors.newSingleThreadScheduledExecutor


  executor.scheduleWithFixedDelay(draw(), 0, 1, TimeUnit.SECONDS)

  def execute(func: Unit => Any): Unit = {
    val startTime = System.currentTimeMillis
    func.apply()
    val endTime = System.currentTimeMillis

    monitor.synchronized {
      count += 1
      elapsedTime += endTime - startTime
    }
  }

  private def draw(): Runnable = new Runnable {
    override def run(): Unit = {
      val (currentCount, currentElapsedTime) = monitor.synchronized {
        (count, elapsedTime)
      }

      if (currentCount > total)
        throw new IllegalStateException("Received more elements than denoted by total!")

      val width = getTerminal.getTerminalWidth

      val itemCounter = s"$currentCount / $total "
      val percentage = f" ${(currentCount.toFloat / total) * 100}%.2f" + "% "

      val millisLeft = currentCount match {
        case 0 => Long.MaxValue
        case _ => (total - currentCount) * (currentElapsedTime.toFloat / currentCount).toLong
      }

      val timeLeft = millisLeft match {
        case seconds if seconds < 60000 => s"${millisLeft / 1000}s"
        case minutes if minutes < 3600000 => s"${millisLeft / 60000}m${(millisLeft % 60000) / 1000}s"
        case tooLong if tooLong > 86400000 => "∞"
        case _ => s"${millisLeft / 3600000}h${(millisLeft % 3600000) / 60000}m${((millisLeft % 3600000) % 60000) / 1000}s"
      }

      val progressBar = width - (itemCounter.length + percentage.length + timeLeft.length + 2) match {
        case size if size > 0 =>
          val progressChars = Math.ceil((currentCount.toFloat / total) * size).toInt match {
            case 0 => 0
            case i => i - 1
          }

          val remainingChars = size - progressChars - 1

          "[" + "=" * progressChars + ">" + "-" * remainingChars + "]"
        case _ => ""
      }

      printer(itemCounter + progressBar + percentage + timeLeft)
    }
  }

  def close(): Unit = {
    executor.shutdown()
    draw().run()
  }
}
