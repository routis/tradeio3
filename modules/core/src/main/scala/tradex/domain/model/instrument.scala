package tradex.domain
package model

import zio.prelude._
import zio.prelude.Assertion._
import io.circe.{ Decoder, Encoder }
import io.circe.generic.semiauto._
import cats.syntax.all._
import java.time.Instant
import squants.market.Money

object instrument {
  object ISINCodeString extends Newtype[String]:
    override inline def assertion: Assertion[String] = hasLength(equalTo(12)) &&
      matches("([A-Z]{2})((?![A-Z]{10}\b)[A-Z0-9]{10})")

  type ISINCodeString = ISINCodeString.Type
  given Decoder[ISINCodeString] = Decoder[String].emap(ISINCodeString.make(_).toEither.leftMap(_.head))
  given Encoder[ISINCodeString] = Encoder[String].contramap(ISINCodeString.unwrap(_))

  case class ISINCode(value: ISINCodeString)
  given Decoder[ISINCode] = deriveDecoder[ISINCode]
  given Encoder[ISINCode] = deriveEncoder[ISINCode]

  case class InstrumentName(value: NonEmptyString)
  given Decoder[InstrumentName] = deriveDecoder[InstrumentName]
  given Encoder[InstrumentName] = deriveEncoder[InstrumentName]

  object LotSizeType extends Subtype[Int]:
    override inline def assertion: Assertion[Int] = greaterThan(0)

  type LotSizeType = LotSizeType.Type
  given Decoder[LotSizeType] = Decoder[Int].emap(LotSizeType.make(_).toEither.leftMap(_.head))
  given Encoder[LotSizeType] = Encoder[Int].contramap(LotSizeType.unwrap(_))

  case class LotSize(value: LotSizeType)
  given Decoder[LotSize] = deriveDecoder[LotSize]
  given Encoder[LotSize] = deriveEncoder[LotSize]

  enum InstrumentType(val entryName: NonEmptyString):
    case CCY extends InstrumentType(NonEmptyString("ccy"))
    case Equity extends InstrumentType(NonEmptyString("equity"))
    case FixedIncome extends InstrumentType(NonEmptyString("fixed income"))

  object InstrumentType:
    given instrumentTypeEncoder: Encoder[InstrumentType] =
      Encoder[String].contramap(_.entryName)

    given instrumentTypeDecoder: Decoder[InstrumentType] =
      Decoder[String].map(InstrumentType.valueOf(_))

  object UnitPriceType extends Subtype[BigDecimal]:
    override inline def assertion: Assertion[BigDecimal] = greaterThan(BigDecimal(0))

  type UnitPriceType = UnitPriceType.Type
  given Decoder[UnitPriceType] = Decoder[BigDecimal].emap(UnitPriceType.make(_).toEither.leftMap(_.head))
  given Encoder[UnitPriceType] = Encoder[BigDecimal].contramap(UnitPriceType.unwrap(_))

  case class UnitPrice(value: UnitPriceType)
  given Decoder[UnitPrice] = deriveDecoder[UnitPrice]
  given Encoder[UnitPrice] = deriveEncoder[UnitPrice]

  final case class InstrumentBase(
      isinCode: ISINCode,
      name: InstrumentName,
      lotSize: LotSize
  )

  trait Instrument:
    private[instrument] def base: InstrumentBase
    def instrumentType: InstrumentType
    val isinCode = base.isinCode
    val name     = base.name
    val lotSize  = base.lotSize

  final case class Ccy(
      private[instrument] base: InstrumentBase
  ) extends Instrument:
    val instrumentType = InstrumentType.CCY

  final case class Equity(
      private[instrument] base: InstrumentBase,
      dateOfIssue: Instant,
      unitPrice: UnitPrice
  ) extends Instrument:
    val instrumentType = InstrumentType.Equity

  final case class FixedIncome(
      private[instrument] base: InstrumentBase,
      dateOfIssue: Instant,
      dateOfMaturity: Option[Instant],
      couponRate: Option[Money],
      couponFrequency: Option[BigDecimal]
  ) extends Instrument:
    val instrumentType = InstrumentType.FixedIncome
}
