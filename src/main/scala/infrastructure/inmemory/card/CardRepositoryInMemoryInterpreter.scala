package infrastructure.inmemory.card

import java.util.concurrent.atomic.AtomicInteger

import cats.Applicative
import domain.card.{Card, CardRepositoryAlgebra}

import scala.collection.mutable

class CardRepositoryInMemoryInterpreter[F[_] : Applicative] extends CardRepositoryAlgebra[F] {

  import cats.syntax.all._

  private val numberGenerator = new AtomicInteger(1000)
  private val cache: mutable.Map[Int, Card] = mutable.Map.empty[Int, Card]

  override def createCard(name: String, amount: Double): Card = {
    val number = numberGenerator.getAndIncrement()
    val card = Card(number, name, amount)
    cache.put(number, card)
    card
  }

  def getCard(number: Int): F[Option[Card]] = cache.get(number).pure[F]

  override def updateBalance(number: Int, amount: Double): F[Unit] =
    cache.get(number)
      .foreach {
        oldCard =>
          cache.put(oldCard.number, oldCard.copy(balance = amount))
      }.pure[F]

}

object CardRepositoryInMemoryInterpreter {
  def apply[F[_] : Applicative](): CardRepositoryInMemoryInterpreter[F] = new CardRepositoryInMemoryInterpreter[F]()
}
