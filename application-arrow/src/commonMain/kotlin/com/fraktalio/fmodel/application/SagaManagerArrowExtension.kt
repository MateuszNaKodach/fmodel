/*
 * Copyright (c) 2023 Fraktalio D.O.O. All rights reserved.
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

package com.fraktalio.fmodel.application

import arrow.core.Either
import arrow.core.raise.either
import com.fraktalio.fmodel.application.Error.ActionResultHandlingFailed
import com.fraktalio.fmodel.application.Error.ActionResultPublishingFailed
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map

/**
 * Extension function - Handles the action result of type [AR].
 *
 * @param actionResult Action Result represent the outcome of some action you want to handle in some way
 * @return [Flow] of [Either] (either [Error] or Actions of type [A])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <AR, A> SagaManager<AR, A>.handleWithEffect(actionResult: AR): Flow<Either<Error, A>> =
    actionResult
        .computeNewActions()
        .publish()
        .map { either<Error, A> { it } }
        .catch { emit(either { raise(ActionResultHandlingFailed(actionResult)) }) }

/**
 * Extension function - Handles the [Flow] of action results of type [AR].
 *
 * @param actionResults Action Results represent the outcome of some action you want to handle in some way
 * @return [Flow] of [Either] (either [Error] or Actions of type [A])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
fun <AR, A> SagaManager<AR, A>.handleWithEffect(actionResults: Flow<AR>): Flow<Either<Error, A>> =
    actionResults
        .flatMapConcat { handleWithEffect(it) }
        .catch { emit(either { raise(ActionResultPublishingFailed(it)) }) }


/**
 * Extension function - Publishes the action result of type [AR] to the saga manager of type  [SagaManager]<[AR], [A]>
 * @receiver action result of type [AR]
 * @param sagaManager of type [SagaManager]<[AR], [A]>
 * @return the [Flow] of [Either] (either [Error] or successfully published Actions of type [A])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <AR, A> AR.publishWithEffect(sagaManager: SagaManager<AR, A>): Flow<Either<Error, A>> =
    sagaManager.handleWithEffect(this)

/**
 * Extension function - Publishes the action result of type [AR] to the saga manager of type  [SagaManager]<[AR], [A]>
 * @receiver [Flow] of action results of type [AR]
 * @param sagaManager of type [SagaManager]<[AR], [A]>
 * @return the [Flow] of [Either] (either [Error] or successfully published Actions of type [A])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
fun <AR, A> Flow<AR>.publishWithEffect(sagaManager: SagaManager<AR, A>): Flow<Either<Error, A>> =
    sagaManager.handleWithEffect(this)

