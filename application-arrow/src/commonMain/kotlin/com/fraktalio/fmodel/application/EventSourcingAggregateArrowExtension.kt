/*
 * Copyright (c) 2022 Fraktalio D.O.O. All rights reserved.
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

import arrow.core.continuations.Effect
import arrow.core.continuations.effect
import com.fraktalio.fmodel.application.Error.CommandHandlingFailed
import com.fraktalio.fmodel.application.Error.CommandPublishingFailed
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Flow] of [Effect] (either [Error] or Events of type [E])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E> EventSourcingAggregate<C, S, E>.handleWithEffect(command: C): Flow<Effect<Error, E>> =
    command
        .fetchEvents()
        .computeNewEvents(command)
        .save()
        .map { effect<Error, E> { it } }
        .catch { emit(effect { shift(CommandHandlingFailed(command)) }) }

/**
 * Extension function - Handles the flow of command messages of type [C]
 *
 * @param commands [Flow] of Command messages of type [C]
 * @return [Flow] of [Effect] (either [Error] or Events of type [E])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E> EventSourcingAggregate<C, S, E>.handleWithEffect(commands: Flow<C>): Flow<Effect<Error, E>> =
    commands
        .flatMapConcat { handleWithEffect(it) }
        .catch { emit(effect { shift(CommandPublishingFailed(it)) }) }

/**
 * Extension function - Publishes the command of type [C] to the event sourcing aggregate of type  [EventSourcingAggregate]<[C], *, [E]>
 * @receiver command of type [C]
 * @param aggregate of type [EventSourcingAggregate]<[C], *, [E]>
 * @return the [Flow] of [Effect] (either [Error] or successfully stored Events of type [E])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, E> C.publishWithEffect(aggregate: EventSourcingAggregate<C, *, E>): Flow<Effect<Error, E>> =
    aggregate.handleWithEffect(this)

/**
 * Extension function - Publishes [Flow] of commands of type [C] to the event sourcing aggregate of type  [EventSourcingAggregate]<[C], *, [E]>
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [EventSourcingAggregate]<[C], *, [E]>
 * @return the [Flow] of [Effect] (either [Error] or successfully stored Events of type [E])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, E> Flow<C>.publishWithEffect(aggregate: EventSourcingAggregate<C, *, E>): Flow<Effect<Error, E>> =
    aggregate.handleWithEffect(this)
