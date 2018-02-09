package infrastructure.db

import cats.Monad
import doobie.util.transactor.Transactor

class JourneyInterpreter[F[_] : Monad](val xa: Transactor[F]) {

}
