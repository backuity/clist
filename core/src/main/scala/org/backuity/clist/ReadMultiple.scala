package org.backuity.clist

trait ReadMultiple[A] { self =>
  def reads(many: Seq[String]): A

  def map[B](f: A => B): ReadMultiple[B] = new ReadMultiple[B] {
    override def reads(many: Seq[String]): B = {
      f(self.reads(many))
    }
  }
}

object ReadMultiple {
  implicit def seqReadMultiple[T: Read]: ReadMultiple[Seq[T]] = new ReadMultiple[Seq[T]] {
    override def reads(many: Seq[String]): Seq[T] = {
      many.map(implicitly[Read[T]].reads)
    }
  }

  implicit def listReadMultiple[T: Read]: ReadMultiple[List[T]] = seqReadMultiple[T].map(_.toList)

  implicit def setReadMultiple[T: Read]: ReadMultiple[Set[T]] = seqReadMultiple[T].map(_.toSet)

  implicit def mapReadMultiple[K: Read, V: Read]: ReadMultiple[Map[K,V]] = seqReadMultiple[(K,V)].map(_.toMap)
}
