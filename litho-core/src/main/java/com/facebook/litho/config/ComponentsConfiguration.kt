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

package com.facebook.litho.config

import android.os.Build
import com.facebook.litho.BuildConfig
import com.facebook.litho.ComponentHost
import com.facebook.litho.ComponentHost.UnsafeModificationPolicy
import com.facebook.litho.ComponentTreeDebugEventListener
import com.facebook.litho.ComponentsLogger
import com.facebook.litho.DefaultErrorEventHandler
import com.facebook.litho.ErrorEventHandler
import com.facebook.litho.config.ComponentsConfiguration.Builder
import com.facebook.litho.perfboost.LithoPerfBoosterFactory
import com.facebook.rendercore.PoolingPolicy
import com.facebook.rendercore.incrementalmount.IncrementalMountExtensionConfigs
import com.facebook.rendercore.visibility.VisibilityBoundsTransformer

/**
 * These values are safe defaults and should not require manual changes.
 *
 * This class hosts all the config parameters that the ComponentTree configures it self .... enable
 * and disable features ... A Component tree uses the [.defaultComponentsConfiguration] by default
 * but a [Builder] can be used to create new instances of the config to override the default
 * parameters ... The default config values can also be overridden by manually setting their values
 * in [.defaultBuilder]
 */
data class ComponentsConfiguration
internal constructor(
    val shouldCacheLayouts: Boolean = true,
    val disableNestedTreeCaching: Boolean = true,
    val shouldAddHostViewForRootComponent: Boolean = false,
    @JvmField
    val useIncrementalMountGapWorker: Boolean = IncrementalMountExtensionConfigs.useGapWorker,
    val useNonRebindingEventHandlers: Boolean = false,
    internal val shouldDisableBgFgOutputs: Boolean = false,
    /**
     * We have detected a scenario where we don't process visibility bounds change if the
     * localVisibleRect goes of the viewport and a LithoView is nested on an Host that is still
     * visible.
     *
     * This option attempts to tackle this issue by attempting to process an extra pass of IM if we
     * detect the Rect became invisible.
     *
     * Check {@code BaseMountingView#isPreviousRectVisibleAndCurrentInvisible} to get more context.
     */
    @JvmField
    val shouldNotifyVisibleBoundsChangeWhenNestedLithoViewBecomesInvisible: Boolean = false,
    /** Whether the [ComponentTree] should be using State Reconciliation. */
    @JvmField val isReconciliationEnabled: Boolean = true,
    /** The handler [ComponentTree] will be used to run the pre-allocation process */
    @JvmField val preAllocationHandler: PreAllocationHandler? = null,
    @JvmField val avoidRedundantPreAllocations: Boolean = false,
    /** Whether the [com.facebook.rendercore.MountState] can be mounted using incremental mount. */
    @JvmField val incrementalMountEnabled: Boolean = true,
    /** Determines the pooling behavior for component hosts */
    @JvmField val componentHostPoolingPolicy: PoolingPolicy = PoolingPolicy.Disabled,
    /**
     * Whether the [com.facebook.LithoView] associated with the [com.facebook.litho.ComponentTree]
     * will process visibility events.
     */
    @JvmField val visibilityProcessingEnabled: Boolean = true,
    /**
     * This class is an error event handler that clients can optionally set on a [ComponentTree] to
     * gracefully handle uncaught/unhandled exceptions thrown from the framework while resolving a
     * layout.
     */
    @JvmField val errorEventHandler: ErrorEventHandler = DefaultErrorEventHandler,
    @JvmField val componentsLogger: ComponentsLogger? = null,
    @JvmField val logTag: String? = if (componentsLogger == null) null else "null",
    /**
     * Determines whether we log, crash, or do nothing if an invalid
     * [com.facebook.litho.ComponentHost] view modification is detected.
     *
     * @see [ComponentHost.UnsafeModificationPolicy]
     */
    @JvmField
    val componentHostInvalidModificationPolicy: ComponentHost.UnsafeModificationPolicy? = null,
    /**
     * You can define a [ComponentTreeDebugEventListener] to listen on specific litho lifecycle
     * related events.
     *
     * @see [com.facebook.litho.debug.LithoDebugEvent]
     * @see [com.facebook.rendercore.debug.DebugEvent]
     */
    @JvmField val debugEventListener: ComponentTreeDebugEventListener? = null,
    @JvmField var enablePreAllocationSameThreadCheck: Boolean = false,
    @JvmField val enableSetLifecycleOwnerTreePropViaDefaultLifecycleOwner: Boolean = false,
    /**
     * LithoViewAttributesExtension is an extension for LithoView that allows setting custom view
     * attributes on the underlying Android View or Drawable. This extension plays a crucial role
     * when working with Litho components because it enables the modification of view properties not
     * already exposed by the Litho framework. The proper functioning of this extension is vital for
     * maintaining correct component behavior during mount and unmount processes, especially when
     * controlled by animations.
     *
     * Prior to the introduction of
     * [com.facebook.litho.LithoViewAttributesExtension.FineGrainedLithoViewAttributesState], an
     * existing bug caused view attributes not to be correctly reset upon unmount due to the lack of
     * information in the view attributes state about the corresponding render unit. This issue
     * arose primarily during animations that controlled the mount/unmount cycle.
     *
     * This configuration aims then to allow you to enable the usage of
     * [com.facebook.litho.LithoViewAttributesExtension.FineGrainedLithoViewAttributesState] to have
     * a more reliable behavior. We are testing this currently to ensure there are no other
     * performance regressions.
     */
    @JvmField val useFineGrainedViewAttributesExtension: Boolean = false,
    /**
     * This is a temporary config param for debugging state list animator crashes during layout of a
     * [ComponentHost]
     */
    @JvmField val cloneStateListAnimators: Boolean = false,
    @JvmField val enableFacadeStateUpdater: Boolean = false,
    @JvmField val skipSecondIsInWorkingRangeCheck: Boolean = false,
    @JvmField val enableVisibilityFixForNestedLithoView: Boolean = false,
    /**
     * This flag is used to enable the use of default item animators in lazy collections, so that
     * the behavior is compatible to what exists nowadays in the
     * [com.facebook.litho.sections.widget.RecyclerCollectionComponent].
     */
    @JvmField val useDefaultItemAnimatorInLazyCollections: Boolean = false,
    /**
     * This defines which strategy we will use to bind the
     * [com.facebook.litho.sections.widget.ExperimentalRecycler].
     *
     * If `null` we will not use the experimental version of a Recycler, and will rely on the
     * MountSpec based one, which is the [com.facebook.litho.widget.RecyclerSpec]
     *
     * @see [PrimitiveRecyclerBinderStrategy] for more details.
     */
    @JvmField val primitiveRecyclerBinderStrategy: PrimitiveRecyclerBinderStrategy? = null,
    /**
     * This flag is used to enable a fix for the issue where components that match the host view
     * size do not get unmounted when they go out of the viewport.
     */
    @JvmField val enableFixForIM: Boolean = false,
    @JvmField val enableLifecycleOwnerWrapper: Boolean = false,
    /**
     * This flag is used to enable a fix for the issue where the Recycler is not measuring taking
     * into any padding specified into it.
     *
     * @see [com.facebok.litho.widget.RecyclerSpec]
     * @see [com.facebook.litho.widget.RecyclerLayoutBehavior]
     */
    @JvmField val measureRecyclerWithPadding: Boolean = false,
    @JvmField val visibilityBoundsTransformer: VisibilityBoundsTransformer? = null,
    @JvmField val sectionsRecyclerViewOnCreateHandler: ((Object) -> Unit)? = null,
    /**
     * Determines whether we should enable stable ids by default in the
     * [com.facebook.litho.widget.RecyclerBinder]
     */
    @JvmField val useStableIdsInRecyclerBinder: Boolean = true,
    /**
     * This will perform an optimization that will verify if the same size specs were used. However,
     * this creates a bug in a specific scenario where double measure happens.
     */
    @JvmField val performExactSameSpecsCheck: Boolean = true
) {

  val shouldAddRootHostViewOrDisableBgFgOutputs: Boolean =
      shouldAddHostViewForRootComponent || shouldDisableBgFgOutputs

  companion object {

    /**
     * This is just a proxy to [LithoDebugConfigurations.isDebugModeEnabled]. We have to keep it
     * until we release a new oss version and we can refer to [LithoDebugConfigurations] directly on
     * Flipper.
     */
    @Deprecated("Use the LithoDebugConfigurations instead")
    var isDebugModeEnabled: Boolean
      get() = LithoDebugConfigurations.isDebugModeEnabled
      set(value) {
        LithoDebugConfigurations.isDebugModeEnabled = value
      }

    /**
     * This is just a proxy to [LithoDebugConfigurations.isDebugHierarchyEnabled]. We have to keep
     * it until we release a new oss version and we can refer to [LithoDebugConfigurations] directly
     * on Flipper.
     */
    @Deprecated("Use the LithoDebugConfigurations instead")
    var isDebugHierarchyEnabled: Boolean
      get() = LithoDebugConfigurations.isDebugHierarchyEnabled
      set(value) {
        LithoDebugConfigurations.isDebugHierarchyEnabled = value
      }

    @JvmField var defaultInstance: ComponentsConfiguration = ComponentsConfiguration()

    /** Indicates that the incremental mount helper is required for this build. */
    @JvmField val USE_INCREMENTAL_MOUNT_HELPER: Boolean = BuildConfig.USE_INCREMENTAL_MOUNT_HELPER

    /** Whether we can access properties in Settings.Global for animations. */
    val CAN_CHECK_GLOBAL_ANIMATOR_SETTINGS: Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1

    /** Whether we need to account for lack of synchronization while accessing Themes. */
    @JvmField
    val NEEDS_THEME_SYNCHRONIZATION: Boolean =
        Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1

    /** The default priority for threads that perform background layout calculations. */
    @JvmField var DEFAULT_BACKGROUND_THREAD_PRIORITY: Int = 5

    /**
     * The default priority for threads that perform background sections change set calculations.
     */
    const val DEFAULT_CHANGE_SET_THREAD_PRIORITY: Int = 0

    /**
     * Populates additional metadata to find mounted components at runtime. Defaults to the presence
     * of an
     *
     * ```
     * IS_TESTING
     * ```
     *
     * system property at startup but can be overridden at runtime.
     */
    @JvmField var isEndToEndTestRun = System.getProperty("IS_TESTING") != null
    @JvmField var isAnimationDisabled = "true" == System.getProperty("litho.animation.disabled")

    /**
     * By default end-to-end tests will disable transitions and this flag lets to explicitly enable
     * transitions to test animation related behavior.
     */
    @JvmField var forceEnableTransitionsForInstrumentationTests: Boolean = false

    @JvmField var enableThreadTracingStacktrace: Boolean = false

    @JvmField var runLooperPrepareForLayoutThreadFactory: Boolean = true

    @JvmField var perfBoosterFactory: LithoPerfBoosterFactory? = null

    /**
     * If true, the [.perfBoosterFactory] will be used to indicate that LayoutStateFuture thread can
     * use the perf boost
     */
    @JvmField var boostPerfLayoutStateFuture: Boolean = false

    /**
     * Start parallel layout of visible range just before serial synchronous layouts in
     * RecyclerBinder
     */
    @JvmField var computeRangeOnSyncLayout: Boolean = false

    /** Keeps the litho layout result tree in the LayoutState. This will increase memory use. */
    @JvmField var keepLayoutResults: Boolean = false

    @JvmField var overlappingRenderingViewSizeLimit: Int = Int.MAX_VALUE
    @JvmField var partialAlphaWarningSizeThresold: Int = Int.MAX_VALUE

    /** Initialize sticky header during layout when its component tree is null */
    @JvmField var initStickyHeaderInLayoutWhenComponentTreeIsNull: Boolean = false

    @JvmField var hostComponentPoolSize: Int = 30

    /** Skip checking for root component and tree-props while layout */
    @JvmField var enableComputeLayoutAsyncAfterInsertion: Boolean = true
    @JvmField var shouldCompareCommonPropsInIsEquivalentTo: Boolean = false
    @JvmField var shouldCompareRootCommonPropsInSingleComponentSection: Boolean = false
    @JvmField var isYogaFlexBasisFixEnabled: Boolean = true

    @JvmField var recyclerBinderStrategy: Int = 0
    @JvmField var shouldOverrideHasTransientState: Boolean = false
    @JvmField var enableFixForDisappearTransitionInRecyclerBinder: Boolean = false
    @JvmField var disableReleaseComponentTreeInRecyclerBinder: Boolean = false
    @JvmField var reduceMemorySpikeUserSession: Boolean = false
    @JvmField var reduceMemorySpikeDataDiffSection: Boolean = false
    @JvmField var reduceMemorySpikeGetUri: Boolean = false
    @JvmField var bindOnSameComponentTree: Boolean = true
    @JvmField var isEventHandlerRebindLoggingEnabled: Boolean = false
    @JvmField var useSafeSpanEndInTextInputSpec: Boolean = false
    @JvmField var useOneShotPreDrawListener: Boolean = false
    @JvmField var useNewCacheValueLogic: Boolean = false
    /**
     * This flag is used to enable logging for the issue where components with an aspect ratio, like
     * NaN or Infinity.
     */
    @JvmField var enableLoggingForInvalidAspectRatio: Boolean = false
    /**
     * This flag is used to enable a change where a state update is skipped if the current and new
     * state value are null.
     */
    @JvmField var enableSkipNullStateUpdates: Boolean = false

    /**
     * This method is only used so that Java clients can have a builder like approach to override a
     * configuration.
     */
    @JvmStatic fun create(): Builder = create(defaultInstance)

    @JvmStatic
    fun create(configuration: ComponentsConfiguration): Builder = Builder(configuration.copy())
  }

  /**
   * This is a builder that only exists so that Java clients can have an easier time creating and
   * overriding specific configurations. For Kotlin one can use directly the named parameters on the
   * [ComponentsConfiguration] constructor.
   */
  class Builder internal constructor(private var baseConfig: ComponentsConfiguration) {

    private var shouldNotifyVisibleBoundsChangeWhenNestedLithoViewBecomesInvisible =
        baseConfig.shouldNotifyVisibleBoundsChangeWhenNestedLithoViewBecomesInvisible
    private var shouldAddHostViewForRootComponent = baseConfig.shouldAddHostViewForRootComponent
    private var shouldCacheLayouts = baseConfig.shouldCacheLayouts
    private var isReconciliationEnabled = baseConfig.isReconciliationEnabled
    private var preAllocationHandler = baseConfig.preAllocationHandler
    private var incrementalMountEnabled = baseConfig.incrementalMountEnabled
    private var componentHostPoolingPolicy = baseConfig.componentHostPoolingPolicy
    private var errorEventHandler = baseConfig.errorEventHandler
    private var componentHostInvalidModificationPolicy =
        baseConfig.componentHostInvalidModificationPolicy
    private var visibilityProcessingEnabled = baseConfig.visibilityProcessingEnabled
    private var logTag = baseConfig.logTag
    private var componentsLogger = baseConfig.componentsLogger
    private var debugEventListener = baseConfig.debugEventListener
    private var enablePreAllocationSameThreadCheck = baseConfig.enablePreAllocationSameThreadCheck
    private var avoidRedundantPreAllocations = baseConfig.avoidRedundantPreAllocations
    private var enableSetLifecycleOwnerTreePropViaDefaultLifecycleOwner =
        baseConfig.enableSetLifecycleOwnerTreePropViaDefaultLifecycleOwner
    private var useFineGrainedViewAttributesExtension =
        baseConfig.useFineGrainedViewAttributesExtension
    private var cloneStateListAnimators = baseConfig.cloneStateListAnimators
    private var enableFacadeStateUpdater = baseConfig.enableFacadeStateUpdater
    private var skipSecondIsInWorkingRangeCheck = baseConfig.skipSecondIsInWorkingRangeCheck
    private var enableVisibilityFixForNestedLithoView =
        baseConfig.enableVisibilityFixForNestedLithoView
    private var useDefaultItemAnimatorInLazyCollections =
        baseConfig.useDefaultItemAnimatorInLazyCollections
    private var primitiveRecyclerBinderStrategy = baseConfig.primitiveRecyclerBinderStrategy
    private var enableFixForIM = baseConfig.enableFixForIM
    private var enableLifecycleOwnerWrapper = baseConfig.enableLifecycleOwnerWrapper
    private var measureRecyclerWithPadding = baseConfig.measureRecyclerWithPadding
    private var visibilityBoundsTransformer = baseConfig.visibilityBoundsTransformer
    private var sectionsRecyclerViewOnCreateHandler: ((Object) -> Unit)? =
        baseConfig.sectionsRecyclerViewOnCreateHandler
    private var useStableIdsInRecyclerBinder = baseConfig.useStableIdsInRecyclerBinder

    fun shouldNotifyVisibleBoundsChangeWhenNestedLithoViewBecomesInvisible(
        enabled: Boolean
    ): Builder = also {
      shouldNotifyVisibleBoundsChangeWhenNestedLithoViewBecomesInvisible = enabled
    }

    fun shouldAddHostViewForRootComponent(enabled: Boolean): Builder = also {
      shouldAddHostViewForRootComponent = enabled
    }

    fun shouldCacheLayouts(enabled: Boolean): Builder = also { shouldCacheLayouts = enabled }

    fun isReconciliationEnabled(enabled: Boolean): Builder = also {
      isReconciliationEnabled = enabled
    }

    fun withPreAllocationHandler(handler: PreAllocationHandler?): Builder = also {
      preAllocationHandler = handler
    }

    fun incrementalMountEnabled(enabled: Boolean): Builder = also {
      incrementalMountEnabled = enabled
    }

    fun componentHostPoolingPolicy(poolingPolicy: PoolingPolicy): Builder = also {
      componentHostPoolingPolicy = poolingPolicy
    }

    fun componentHostInvalidModificationPolicy(
        invalidModificationPolicy: UnsafeModificationPolicy?
    ): Builder = also { componentHostInvalidModificationPolicy = invalidModificationPolicy }

    fun enableVisibilityProcessing(enabled: Boolean): Builder = also {
      visibilityProcessingEnabled = enabled
    }

    fun errorEventHandler(handler: ErrorEventHandler): Builder = also {
      errorEventHandler = handler
    }

    fun logTag(tag: String?): Builder = also { logTag = tag }

    fun componentsLogger(componentsLogger: ComponentsLogger?): Builder = also {
      this.componentsLogger = componentsLogger
    }

    fun debugEventListener(debugEventListener: ComponentTreeDebugEventListener?) {
      this.debugEventListener = debugEventListener
    }

    fun enablePreAllocationSameThreadCheck(value: Boolean): Builder = also {
      enablePreAllocationSameThreadCheck = value
    }

    fun avoidRedundantPreAllocations(value: Boolean): Builder = also {
      avoidRedundantPreAllocations = value
    }

    fun primitiveRecyclerBinderStrategy(
        primitiveRecyclerBinderStrategy: PrimitiveRecyclerBinderStrategy?
    ): Builder = also { this.primitiveRecyclerBinderStrategy = primitiveRecyclerBinderStrategy }

    fun enableSetLifecycleOwnerTreePropViaDefaultLifecycleOwner(
        enableSetLifecycleOwnerTreePropViaDefaultLifecycleOwner: Boolean
    ): Builder = also {
      this.enableSetLifecycleOwnerTreePropViaDefaultLifecycleOwner =
          enableSetLifecycleOwnerTreePropViaDefaultLifecycleOwner
    }

    fun useFineGrainedViewAttributesExtension(enabled: Boolean): Builder = also {
      useFineGrainedViewAttributesExtension = enabled
    }

    fun cloneStateListAnimators(enabled: Boolean): Builder = also {
      cloneStateListAnimators = enabled
    }

    fun enableFacadeStateUpdater(enabled: Boolean): Builder = also {
      enableFacadeStateUpdater = enabled
    }

    fun skipSecondIsInWorkingRangeCheck(enabled: Boolean): Builder = also {
      skipSecondIsInWorkingRangeCheck = enabled
    }

    fun enableVisibilityFixForNestedLithoView(enabled: Boolean): Builder = also {
      enableVisibilityFixForNestedLithoView = enabled
    }

    fun useDefaultItemAnimatorInLazyCollections(enabled: Boolean): Builder = also {
      useDefaultItemAnimatorInLazyCollections = enabled
    }

    fun enableFixForIM(enabled: Boolean): Builder = also { enableFixForIM = enabled }

    fun enableLifecycleOwnerWrapper(enabled: Boolean): Builder = also {
      enableLifecycleOwnerWrapper = enabled
    }

    fun measureRecyclerWithPadding(enabled: Boolean): Builder = also {
      measureRecyclerWithPadding = enabled
    }

    fun visibilityBoundsTransformer(transformer: VisibilityBoundsTransformer?): Builder = also {
      visibilityBoundsTransformer = transformer
    }

    fun sectionsRecyclerViewOnCreateHandler(handler: ((Object) -> Unit)?): Builder = also {
      sectionsRecyclerViewOnCreateHandler = handler
    }

    fun useStableIdsInRecyclerBinder(enabled: Boolean): Builder = also {
      useStableIdsInRecyclerBinder = enabled
    }

    fun build(): ComponentsConfiguration {
      return baseConfig.copy(
          shouldCacheLayouts = shouldCacheLayouts,
          shouldAddHostViewForRootComponent = shouldAddHostViewForRootComponent,
          isReconciliationEnabled = isReconciliationEnabled,
          preAllocationHandler = preAllocationHandler,
          incrementalMountEnabled = incrementalMountEnabled,
          componentHostPoolingPolicy = componentHostPoolingPolicy,
          componentHostInvalidModificationPolicy = componentHostInvalidModificationPolicy,
          visibilityProcessingEnabled = visibilityProcessingEnabled,
          shouldNotifyVisibleBoundsChangeWhenNestedLithoViewBecomesInvisible =
              shouldNotifyVisibleBoundsChangeWhenNestedLithoViewBecomesInvisible,
          errorEventHandler = errorEventHandler,
          logTag =
              if (logTag == null && componentsLogger != null) {
                "null"
              } else {
                logTag
              },
          componentsLogger = componentsLogger,
          debugEventListener = debugEventListener,
          enablePreAllocationSameThreadCheck = enablePreAllocationSameThreadCheck,
          avoidRedundantPreAllocations = avoidRedundantPreAllocations,
          primitiveRecyclerBinderStrategy = primitiveRecyclerBinderStrategy,
          enableSetLifecycleOwnerTreePropViaDefaultLifecycleOwner =
              enableSetLifecycleOwnerTreePropViaDefaultLifecycleOwner,
          useFineGrainedViewAttributesExtension = useFineGrainedViewAttributesExtension,
          cloneStateListAnimators = cloneStateListAnimators,
          enableFacadeStateUpdater = enableFacadeStateUpdater,
          skipSecondIsInWorkingRangeCheck = skipSecondIsInWorkingRangeCheck,
          enableVisibilityFixForNestedLithoView = enableVisibilityFixForNestedLithoView,
          useDefaultItemAnimatorInLazyCollections = useDefaultItemAnimatorInLazyCollections,
          enableFixForIM = enableFixForIM,
          enableLifecycleOwnerWrapper = enableLifecycleOwnerWrapper,
          measureRecyclerWithPadding = measureRecyclerWithPadding,
          visibilityBoundsTransformer = visibilityBoundsTransformer,
          sectionsRecyclerViewOnCreateHandler = sectionsRecyclerViewOnCreateHandler,
          useStableIdsInRecyclerBinder = useStableIdsInRecyclerBinder)
    }
  }
}
