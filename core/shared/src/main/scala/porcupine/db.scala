/*
 * Copyright 2023 Arman Bilge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package porcupine

import cats.effect.kernel.Resource

abstract class Database[F[_]] private[porcupine]:
  def prepare[A, B](query: Query[A, B]): Resource[F, Statement[F, A, B]]

object Database extends DatabasePlatform

abstract class Statement[F[_], A, B] private[porcupine]:
  def cursor(args: A): Resource[F, Cursor[F, B]]

abstract class Cursor[F[_], A] private[porcupine]:
  def fetch(maxRows: Int): F[(List[A], Boolean)]
