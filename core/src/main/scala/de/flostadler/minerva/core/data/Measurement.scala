package de.flostadler.minerva.core.data

import com.fasterxml.jackson.annotation.JsonUnwrapped

case class Measurement[A](timestamp: Long, @JsonUnwrapped value: A)
