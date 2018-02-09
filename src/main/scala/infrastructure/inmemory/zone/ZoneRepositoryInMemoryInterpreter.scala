package infrastructure.inmemory.zone

import cats.Applicative
import domain.zone.ZoneRepositoryAlgebra

case class Station(code: String, name: String, zones: List[Int])

class ZoneRepositoryInMemoryInterpreter[F[_] : Applicative] extends ZoneRepositoryAlgebra[F] {

  import cats.syntax.all._

  private val cache = Map(
    "HOL" -> Station("HOL", "Holborn", List(1)),
    "EAR" -> Station("EAR", "Earl’s Court", List(1, 2)),
    "HAM" -> Station("HAM", "Hammersmith", List(2)),
    "WIM" -> Station("WIM", "Wimbledon", List(3)),
    "EDG" -> Station("EDG", "Edgware", List(5))
  )

  override def getZonesByCode(stationCode: String): F[List[Int]] =
    cache.get(stationCode.toUpperCase)
      .map(_.zones.sorted)
      .toList
      .flatten
      .pure[F]
}

object ZoneRepositoryInMemoryInterpreter {
  def apply[F[_] : Applicative](): ZoneRepositoryInMemoryInterpreter[F] = new ZoneRepositoryInMemoryInterpreter[F]()
}
