package domain.zone

trait ZoneRepositoryAlgebra[F[_]] {
  def getZonesByCode(stationCode: String): F[List[Int]]
}
