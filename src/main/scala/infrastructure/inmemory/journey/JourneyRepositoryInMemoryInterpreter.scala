package infrastructure.inmemory.journey

import cats.Applicative
import cats.implicits._
import domain.barrier.Barrier
import domain.barrier.Type.Tube
import domain.journey.JourneyRepositoryAlgebra

import scala.collection.mutable.ListBuffer


class JourneyRepositoryInMemoryInterpreter[F[_] : Applicative] extends JourneyRepositoryAlgebra[F] {
  private val cache = ListBuffer[(Int, Barrier)]()

  override def addJourney(cardNumber: Int, barrier: Barrier): F[Unit] =
    ((cardNumber, barrier) +=: cache)
      .pure[F].map(_ => ())

  override def getMostRecentTubeBarrier(cardNumber: Int): F[Option[Barrier]] =
    cache.find {
      case (number, barrier) => number == cardNumber && barrier.`type` == Tube
    }.map(_._2).pure[F]

  override def journeyHistory(cardNumber: Int): F[List[Barrier]] =
    cache.collect {
      case (_, barrier) => barrier
    }.toList.reverse.pure[F]
}

object JourneyRepositoryInMemoryInterpreter {
  def apply[F[_] : Applicative](): JourneyRepositoryAlgebra[F] = new JourneyRepositoryInMemoryInterpreter[F]()
}
