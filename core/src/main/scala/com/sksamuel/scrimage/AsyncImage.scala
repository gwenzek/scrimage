/*
   Copyright 2013 Stephen K Samuel

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.sksamuel.scrimage

import java.io.{ File, InputStream }

import scala.concurrent._
import scala.concurrent.duration._
import scala.util.Try

/** @author Stephen Samuel */
class AsyncImage(val image: Future[Image])(implicit executionContext: ExecutionContext) extends ImageFunctor[Image] with Future[Image] {
  type Self = AsyncImage
  def fmap(f: Image => Image) = new AsyncImage(image.map(f))(executionContext)

  def isCompleted: Boolean = image.isCompleted

  def value: Option[Try[Image]] = image.value

  @scala.throws[Exception](classOf[Exception])
  def result(atMost: Duration)(implicit permit: CanAwait): Image = image.result(atMost)(permit)

  @scala.throws[InterruptedException](classOf[InterruptedException])
  @scala.throws[TimeoutException](classOf[TimeoutException])
  def ready(atMost: Duration)(implicit permit: CanAwait): this.type = {
    image.ready(atMost)(permit)
    this
  }

  def onComplete[U](f: (Try[Image]) => U)(implicit executor: ExecutionContext): Unit = image.onComplete(f)(executor)

  def apply[B](f: (Image) => B): B = Await.result(image.map(f), AsyncImage.MAX_WAIT)

  def toImage: Image = Await.result(image, AsyncImage.MAX_WAIT)
}

object AsyncImage {

  private val MAX_WAIT = 10.minutes

  def apply(bytes: Array[Byte])(implicit executionContext: ExecutionContext): Future[AsyncImage] = Future {
    Image(bytes).toAsync
  }
  def apply(in: InputStream)(implicit executionContext: ExecutionContext): Future[AsyncImage] = Future {
    Image(in).toAsync
  }
  def apply(file: File)(implicit executionContext: ExecutionContext): Future[AsyncImage] = Future {
    Image(file).toAsync
  }
  def apply(image: Image)(implicit executionContext: ExecutionContext) = new AsyncImage(Future(image))
  def apply(image: AsyncImage)(implicit executionContext: ExecutionContext) = image
  def apply(image: ImageLikeWithMeta[Image])(implicit executionContext: ExecutionContext): ImageLikeWithMeta[AsyncImage] =
    ImageWithMeta(new AsyncImage(Future(image.toImage)), image.metadata)
  def apply(image: ImageLike)(implicit executionContext: ExecutionContext) = new AsyncImage(Future(image.toImage))
}
