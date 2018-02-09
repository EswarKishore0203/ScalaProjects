package domain


sealed trait ValidationError extends Product with Serializable{
  val message: String
}

case object BarrierInError extends ValidationError {
  val message: String = "You can't tap in when you did not tap out"
}

case object MinimumBalanceError extends ValidationError {
  override val message: String = "You don't enough balance to travel. Please load money on you card"
}


case object BarrierOutError extends ValidationError {
  val message: String = "You can't tap out when you never tapped in"
}