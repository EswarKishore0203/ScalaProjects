package domain.barrier

import cats.effect.IO
import domain.barrier.Type.{Bus, Tube}
import domain.card.Card
import domain.{BarrierInError, BarrierOutError, MinimumBalanceError, ValidationError}
import org.scalatest.Outcome
import util.UnitSpec

class BarrierServiceSpec
  extends UnitSpec {
  type FixtureParam = SetUp[IO]
  val plannedTrip =
    """
      -Tube Holborn to Earl’s Court
      -328 bus from Earl’s Court to Chelsea
      -Tube Earl’s court to Hammersmith"""

  override def withFixture(test: OneArgTest): Outcome = test(new SetUp[IO]())

  "You" must s"complete your trip $plannedTrip in £6.3 " in { fixture =>
    val result: Either[ValidationError, (Card, Barrier)] = plannedTrip(fixture)
    result should be('right)
    val (card, _) = result.right.value
    card.balance shouldBe 23.7
  }


  "You" can "not tap out when you never tapped in" in { fixture =>
    val card = fixture.createCard(30)
    val previous = fixture.tube("HOL", Direction.OUT)
    val current = fixture.tube("EAR", Direction.OUT)
    val result = for {
      firstJ <- fixture.cardService.makeJourney(previous, card).unsafeRunSync()
      finished <- fixture.cardService.makeJourney(current, firstJ._1).unsafeRunSync()
    } yield finished

    result should be('left)
    result.left.value shouldBe BarrierOutError
  }

  "You" can "not tap in because you didn't tapped out last journey" in { fixture =>
    val card = fixture.createCard(30)
    val previous = fixture.tube("HOL", Direction.IN)
    val current = fixture.tube("EAR", Direction.IN)
    val result = for {
      firstJ <- fixture.cardService.makeJourney(previous, card).unsafeRunSync()
      finished <- fixture.cardService.makeJourney(current, firstJ._1).unsafeRunSync()
    } yield finished

    result should be('left)
    result.left.value shouldBe BarrierInError
  }

  it should "charge maximum fare if user didn't tapped out after tap in" in { fixture =>
    val cardService = fixture.cardService
    val card = fixture.createCard(30)
    val holIn = fixture.tube("HOL", Direction.IN)
    val earlsCourtOut = fixture.tube("EAR", Direction.OUT)
    val hammerSmithOut = fixture.tube("HAM", Direction.IN)
    val result = for {
      firstJ <- cardService.makeJourney(holIn, card).unsafeRunSync()
      secondJ <- cardService.makeJourney(earlsCourtOut, firstJ._1).unsafeRunSync()
      finished <- cardService.makeJourney(hammerSmithOut, secondJ._1).unsafeRunSync()
    } yield finished

    result should be('right)
    result.right.value._1.balance shouldBe 24.3
  }

  "Barrier" should "display an error message if balance is less than maximum fare(£3.2) for tube" in { fixture =>
    val card = fixture.createCard(2)
    val transaction = fixture.barrierValidation.minimumBalanceForTravel(card, Tube).unsafeRunSync()
    transaction should be('left)
    transaction.left.value shouldBe MinimumBalanceError
  }

  "Barrier" should "display an error message if balance is less than maximum fare(£1.8) for bus" in { fixture =>
    val card = fixture.createCard(1.5)
    val transaction = fixture.barrierValidation.minimumBalanceForTravel(card, Bus).unsafeRunSync()
    transaction should be('left)
    transaction.left.value shouldBe MinimumBalanceError
  }

  "Barrier" should "not allow to travel if balance is less than maximum fare(£3.2)" in { fixture =>
    val cardService = fixture.cardService
    val card = fixture.createCard(2)
    val holIn = fixture.tube("HOL", Direction.IN)
    val result = cardService.makeJourney(holIn, card).unsafeRunSync()
    result should be('left)
    result.left.value shouldBe MinimumBalanceError
  }

  "When the user passes through the inward barrier at the station, their oyster card" should "be charge maximum fare" in { fixture =>
    val cardService = fixture.cardService
    val card = fixture.createCard(12)
    val holIn = fixture.tube("HOL", Direction.IN)
    val entry = cardService.makeJourney(holIn, card).unsafeRunSync()
    val (updatedCard, _) = entry.right.value
    updatedCard.balance shouldBe 8.8
    val wimOut = fixture.tube("WIM", Direction.OUT)
    val exit = cardService.makeJourney(wimOut, updatedCard).unsafeRunSync()
    exit.right.value._1.balance shouldBe 8.8
  }

  "When the user passes any three zones, it" should "be charge maximum fare" in { fixture =>
    val card = fixture.createCard(15)
    val exit: Either[ValidationError, (Card, Barrier)] = makeJourney(card, fixture)
    exit.right.value._1.balance shouldBe 11.8
  }

  "User" should " be able to see history journey" in { fixture =>
    val card = fixture.createCard(15)
    makeJourney(card, fixture)
    val barriers = fixture.cardService.transactionHistory(card).unsafeRunSync()
    barriers should have size 2
  }

  private def makeJourney(card: Card, fixture: SetUp[IO]) = {
    val cardService = fixture.cardService

    val holIn = fixture.tube("HOL", Direction.IN)
    val entry = cardService.makeJourney(holIn, card).unsafeRunSync()
    val (updatedCard, _) = entry.right.value
    val edgOut = fixture.tube("EDG", Direction.OUT)
    val exit = cardService.makeJourney(edgOut, updatedCard).unsafeRunSync()
    exit
  }

  private def plannedTrip(fixture: SetUp[IO]) = {
    val cardService = fixture.cardService
    val card = fixture.createCard(30)
    val holIn = fixture.tube("HOL", Direction.IN)
    val earlsCourtOut = fixture.tube("EAR", Direction.OUT)
    val `328bus` = fixture.bus("328 Bus")
    val earlsCourtIn = earlsCourtOut.copy(direction = Direction.IN)
    val hammerSmithOut = fixture.tube("HAM", Direction.OUT)

    val result = for {
      firstJ <- cardService.makeJourney(holIn, card).unsafeRunSync()
      secondJ <- cardService.makeJourney(earlsCourtOut, firstJ._1).unsafeRunSync()
      thirdJ <- cardService.makeJourney(`328bus`, secondJ._1).unsafeRunSync()
      fourthJ <- cardService.makeJourney(earlsCourtIn, thirdJ._1).unsafeRunSync()
      finished <- cardService.makeJourney(hammerSmithOut, fourthJ._1).unsafeRunSync()
    } yield finished
    result
  }
}

