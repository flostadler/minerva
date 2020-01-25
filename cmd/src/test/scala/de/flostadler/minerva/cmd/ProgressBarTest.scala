package de.flostadler.minerva.cmd;

import org.scalatest.flatspec.AnyFlatSpec;

class ProgressBarTest extends AnyFlatSpec {

  "progress bar" should " print to command line" in {
    resource.managed { EnhancedProgressBar(5000, println(_)) }
      .acquireAndGet(progressBar => {
        for (_ <- 0 until 5000) {
          progressBar execute { _ => Thread.sleep(10) }
        }
      })
  }
}