package domain.card

case class Card(number: Int, ownerName: String, balance: Double) {
  def update(amount: Double): Card = copy(balance = balance - amount)
}
