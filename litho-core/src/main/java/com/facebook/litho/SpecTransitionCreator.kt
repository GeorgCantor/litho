/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
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

package com.facebook.litho

import com.facebook.litho.internal.HookKey
import com.facebook.litho.transition.TransitionCreator
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

internal class SpecTransitionCreator(private val scopedComponentInfo: ScopedComponentInfo) :
    TransitionCreator {
  override val identityKey: HookKey = HookKey(scopedComponentInfo.context.globalKey, 0)

  override val supportsOptimisticTransitions: Boolean
    get() = false

  override fun createTransition(previous: Component.RenderData?): Transition? {
    val context = scopedComponentInfo.context
    val component = scopedComponentInfo.component
    require(isPreviousRenderDataSupported(component)) {
      "Trying to apply previous render data to component that doesn't support it"
    }
    component.applyPreviousRenderData(previous)
    return component.createTransition(context)
  }

  override fun recordRenderData(): Component.RenderData {
    val context = scopedComponentInfo.context
    val component = scopedComponentInfo.component
    require(isPreviousRenderDataSupported(component)) {
      "Trying to record previous render data to component that doesn't support it"
    }
    val renderData = component.recordRenderData(context, null)
    // This method is only invoked for SpecGeneratedComponents whose RenderData is never null
    return requireNotNull(renderData)
  }

  @OptIn(ExperimentalContracts::class)
  private fun isPreviousRenderDataSupported(component: Component): Boolean {
    contract { returns(true) implies (component is SpecGeneratedComponent) }
    return component is SpecGeneratedComponent && component.needsPreviousRenderData()
  }
}
