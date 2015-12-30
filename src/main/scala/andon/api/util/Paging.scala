package andon.api.util

import cats.data.Xor
import scalikejdbc._

sealed abstract class SortOrder {
  def sql[A](s: SQLBuilder[A]): SQLBuilder[A]
  def sql(s: SQLSyntax): SQLSyntax
}
final case object DESC extends SortOrder {
  def sql[A](s: SQLBuilder[A]): SQLBuilder[A] = s.append(sqls.desc)
  def sql(s: SQLSyntax): SQLSyntax = s.desc
}
final case object ASC extends SortOrder {
  def sql[A](s: SQLBuilder[A]): SQLBuilder[A] = s.append(sqls.desc)
  def sql(s: SQLSyntax): SQLSyntax = s.asc
}
object SortOrder extends Injective[SortOrder, String] {
  def to(s: SortOrder) = s match {
    case DESC => "DESC"
    case ASC => "ASC"
  }
  val domain: Set[SortOrder] = Set(DESC, ASC)
}

final case class Paging(
  offset: Option[Int] = None,
  limit: Option[Int] = None,
  order: Option[Seq[(SQLSyntax, Option[SortOrder])]] = None
) {
  def defaultOffset(offset: Int): Paging =
    this.copy(offset = this.offset.orElse(Some(offset)))
  def removeOffset: Paging = this.copy(offset = None)
  def defaultLimit(limit: Int): Paging =
    this.copy(limit = this.limit.orElse(Some(limit)))
  def maxLimit(max: Int): Paging =
    this.copy(limit = this.limit.map(_.min(max)).orElse(Some(max)))
  def removeLimit: Paging = this.copy(limit = None)
  def defaultOrder(order: (SQLSyntax, SortOrder)*): Paging =
    this.copy(order = this.order.orElse(Some(order.map(o => o.copy(_2 = Some(o._2))))))
  def removeOrder: Paging = this.copy(order = None)
  def subQuerySql[A](sql: SubQuerySQLBuilder[A]): SubQuerySQLBuilder[A] = {
    val pagingSql = {
      val sorted = order.map { os =>
        val ss = os.map { case (by, o) => o.fold(by)(_.sql(by)) }
        sqls.orderBy(ss: _*)
      }.getOrElse(sqls.empty)
      val limitted = limit.map(sorted.limit).getOrElse(sorted)
      offset.map(limitted.offset).getOrElse(limitted)
    }
    sql.append(pagingSql).asInstanceOf[SubQuerySQLBuilder[A]]
  }
  def sql[A](sql: SQLBuilder[A]): SQLBuilder[A] = {
    val pagingSql = {
      val sorted = order.map { os =>
        val ss = os.map { case (by, o) => o.fold(by)(_.sql(by)) }
        sqls.orderBy(ss: _*)
      }.getOrElse(sqls.empty)
      val limitted = limit.map(sorted.limit).getOrElse(sorted)
      offset.map(limitted.offset).getOrElse(limitted)
    }
    sql.append(pagingSql)
  }
}
