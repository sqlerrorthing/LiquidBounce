<script lang="ts">
    import {portal} from "../../../../../util/utils";
    import {scaleFactor} from "../../../clickgui_store";
    import {onDestroy, onMount} from "svelte";
    import {backOut} from "svelte/easing";
    import {scale} from "svelte/transition"

    export let text;

    let hovered = false

    let iconRef: HTMLElement;
    let iconPosition = { top: 0, left: 0, width: 0, height: 0 };
    const resizeObserver = new ResizeObserver(updatePosition);

    function updatePosition() {
        if (!iconRef) return;
        const rect = iconRef.getBoundingClientRect();
        iconPosition = {
            top: rect.top + window.scrollY,
            left: rect.left + window.scrollX,
            width: rect.width,
            height: rect.height
        };
    }

    onMount(() => {
        window.addEventListener('resize', updatePosition);
        if (iconRef) resizeObserver.observe(iconRef);
        updatePosition();
    })

    onDestroy(() => {
        window.removeEventListener('resize', updatePosition);
        resizeObserver.disconnect();
    });

    $: {
        if (hovered) {
            updatePosition()
        }
    }
</script>

<span role="button"
      class="tooltip"
      tabindex="0"
      onmouseenter={() => hovered = true}
      onmouseleave={() => hovered = false}
>
    <img bind:this={iconRef} src="/img/clickgui/icon-question-mark.svg" alt="">
    {#if hovered}
        <span class="text"
              use:portal
              transition:scale={{duration: 200, easing: backOut, start: 0.9}}
              style="transform: translate(-50%, -100%) scale({$scaleFactor * 50}%);
                    top: {iconPosition.top - 15}px;
                    left: {iconPosition.left + iconPosition.width / 2}px"
        >
            {text}
        </span>
    {/if}
</span>

<style lang="scss">
  @use "../../../../../colors.scss" as *;
  @use "sass:color";

  img {
    width: 10px;
    height: auto;
    margin-left: 2px;
  }

  .tooltip {
    position: relative;
  }

  .text {
    position: absolute;
    z-index: 99999;
    color: $clickgui-text-color;
    width: 150px;
    border-radius: 3px;
    padding: 2px;
    background-color: rgba($clickgui-base-color, 0.85);
    outline: 1px solid color.adjust($clickgui-text-color, $lightness: -85%);
    font-size: 12px;
  }
</style>
