package andon.api.models

import cats.data.Xor
import scalikejdbc._
import org.joda.time.DateTime

import andon.api.errors._
import andon.api.models.generated._
import andon.api.util.ClassId

object ClassModel extends ClassModel
trait ClassModel {

  private val c = Class.c
  private val p = Prize.p
  private val cpr = ClassPrizeRel.cpr
  private val ct = ClassTag.ct

  def prizeOpt(p: SyntaxProvider[Prize])(rs: WrappedResultSet): Option[Prize] =
    rs.shortOpt(p.resultName.id).map(_ => Prize(p)(rs))
  def classTagOpt(ct: SyntaxProvider[ClassTag])(rs: WrappedResultSet): Option[ClassTag] =
    rs.intOpt(ct.resultName.id).map(_ => ClassTag(ct)(rs))

  def findId(classId: ClassId)(implicit s: DBSession): Option[Short] = {
    withSQL {
      select(c.result.id).from(Class as c).where
        .eq(c.times, classId.times.raw).and
        .eq(c.grade, classId.grade).and
        .eq(c.`class`, classId.`class`)
    }.map(_.short(c.resultName.id)).single.apply()
  }

  def findWithPrizesAndTags(classId: ClassId)(implicit s: DBSession): Option[(Class, Seq[Prize], Seq[String])] = {
    withSQL {
      select.from(Class as c)
        .leftJoin(ClassPrizeRel as cpr).on(c.id, cpr.classId)
        .leftJoin(Prize as p).on(cpr.prizeId, p.id)
        .leftJoin(ClassTag as ct).on(c.id, ct.classId)
        .where
        .eq(c.times, classId.times.raw).and
        .eq(c.grade, classId.grade).and
        .eq(c.`class`, classId.`class`)
    }.one(Class(c))
      .toManies(prizeOpt(p), classTagOpt(ct))
      .map { (c, ps, cts) => (c, ps, cts.map(_.label)) }
      .single
      .apply()
  }

  def create(
    classId: ClassId,
    title: String, titleKana: Option[String] = None,
    description: Option[String] = None, score: Option[BigDecimal] = None,
    headerImageUrl: Option[String] = None, thumbnailUrl: Option[String] = None
  )(implicit s: DBSession): Xor[AndonError, Class] = {
    val now = DateTime.now
    try {
      val c = Class.create(
        times = classId.times.raw, grade = classId.grade, `class` = classId.`class`,
        title = title, titleKana = titleKana,
        description = description, score = score,
        headerImageUrl = headerImageUrl, thumbnailUrl = thumbnailUrl,
        createdAt = now, updatedAt = now
      )
      Xor.right(c)
    } catch {
      case e: java.sql.SQLException => Xor.left(Incorrect(e.getMessage))
    }
  }
}
