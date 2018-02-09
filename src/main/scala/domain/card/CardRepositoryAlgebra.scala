package domain.card

trait CardRepositoryAlgebra[F[_]] {
  def createCard(name: String, amount: Double): Card

  def getCard(number: Int): F[Option[Card]]

  def updateBalance(number: Int, amount: Double): F[Unit]
}
