package de.flostadler.minerva.cmd

import java.util.concurrent.{Executors, TimeUnit}
import java.util.concurrent.atomic.AtomicLong

import jline.Terminal.getTerminal


object ProgressBar {
  def apply(count: Long): ProgressBar = new ProgressBar(count)
}

class ProgressBar(val total: Long) extends AutoCloseable {
  private val current = new AtomicLong(0)
  private val executor = Executors.newSingleThreadScheduledExecutor
  private val startTime = System.currentTimeMillis

  executor.scheduleWithFixedDelay(draw(), 0, 1, TimeUnit.SECONDS)

  def +=(i: Long): Long = current.addAndGet(i)
  def ++ : Long = this += 1

  def execute(func: Unit => Any): Unit = {
    func.apply()
    ++
  }

  private def draw(): Runnable = new Runnable {
    override def run(): Unit = {
      val currentCount = current.get

      if (currentCount > total)
        throw new IllegalStateException("Received more elements than denoted by total!")

      val currentTime = System.currentTimeMillis + 1
      val width = getTerminal.getTerminalWidth

      val itemsPerMilli = currentCount.toFloat / (currentTime - startTime)

      val millisLeft = Math.ceil((total - currentCount) / itemsPerMilli).toLong

      val itemCounter = s"$currentCount / $total "
      val percentage = f" ${(currentCount.toFloat / total)*100}%.2f" + "% "

      val timeLeft = millisLeft match {
        case seconds if seconds < 60000 => s"${millisLeft/1000}s"
        case minutes if minutes < 3600000 => s"${millisLeft/60000}m${(millisLeft%60000)/1000}s"
        case tooLong if tooLong > 86400000 => "∞"
        case _ => s"${millisLeft/3600000}h${(millisLeft%3600000)/60000}m${((millisLeft%3600000)%60000)/1000}s"
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

      print("\r" + itemCounter + progressBar + percentage + timeLeft)
    }
  }

  def close(): Unit = {
    executor.shutdown()
    draw().run()
  }
}
