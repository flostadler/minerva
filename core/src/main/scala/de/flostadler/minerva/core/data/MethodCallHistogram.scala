package de.flostadler.minerva.core.data

case class MethodCallHistogram(
                                methodName: String,
                                className: String,
                                count: Long,
                                sum: Double,
                                min: Double,
                                max: Double
                              )
