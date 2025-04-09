<script lang="ts">
    import type {MaxStacksGroup} from "../../../../../../integration/types";
    import noUiSlider, {type API} from "nouislider";
    import ValueInput from "../../../common/ValueInput.svelte";
    import {createEventDispatcher, onMount} from "svelte";
    import ItemGroupSelector from "../ItemGroupSelector.svelte";

    export let group: MaxStacksGroup;

    const dispatch = createEventDispatcher();

    let slider: HTMLElement;
    let apiSlider: API;

    onMount(() => {
        apiSlider = noUiSlider.create(slider, {
            start: group.itemCount,
            connect: "lower",
            range: {
                'min': [0, 1],
                '15%': [5, 1],
                '30%': [16, 16],
                '50%': [2 * 64, 64],
                '75%': [5 * 64, 64],
                'max': [64 * 9 * 4],
            },
            // pips: {
            //     mode: 'range',
            //     density: 4,
            //     format: {
            //         to: (value) => {
            //             if (value >= 64 * 9 * 4) {
            //                 return "∞";
            //             }
            //             if (value < 64) {
            //                 return value.toString();
            //             }
            //
            //             let x = (value / 64) | 0;
            //
            //             return `64 x ${x}`;
            //         }
            //     }
            // },
            step: 1,
        });

        apiSlider.on("update", (values) => {
            updateItemCount(0, parseInt(values[0].toString()));
        });

        apiSlider.on("set", () => {
            dispatch("change");
        });
    });

    function handleChange() {
        dispatch("change")
    }

    function handleDelete() {
        dispatch("delete")
    }

    let nStacks: number = 0;
    let nItems: number = group.itemCount;

    function updateItemCount(s: number = nStacks, i: number = nItems) {
        let totalItemCount = s * 64 + i;

        group.itemCount = totalItemCount;

        let totalStacks = (totalItemCount / 64) | 0;
        let leftItems = (totalItemCount % 64) | 0;

        nStacks = totalStacks;
        nItems = leftItems;
    }
</script>

<!-- svelte-ignore a11y-click-events-have-key-events -->
<!-- svelte-ignore a11y-no-static-element-interactions -->
<!-- svelte-ignore a11y-no-noninteractive-element-interactions -->
<div class="container-wrapper">
    <div class="container">
        <ItemGroupSelector bind:items={group.items} on:change={handleChange} style="width: 309px; max-height: 70px;"/>

        <div class="slider-wrapper">
            <div class="slider-container">
                <div class="slider-top">
                    <span class="slider-left">Limit</span>
                    <div class="slider-right">
                        {#if group.itemCount < 9 * 4 * 64}
                            <span style="color: {group.itemCount < 64 ? 'gray' : 'white'}">64 x </span>
                            <ValueInput valueType="int" bind:value={nStacks}
                                        on:change={() => updateItemCount()}/>
                            <span> + </span>
                            <ValueInput valueType="int" bind:value={nItems}
                                        on:change={() => updateItemCount()}/>
                        {:else}
                            &infin;
                        {/if}
                    </div>
                </div>
                <div bind:this={slider} class="slider"></div>
            </div>
        </div>
    </div>
    <div class="delete">
        <img src="img/menu/icon-exit-danger.svg" alt="exit" on:click={handleDelete}>
    </div>
</div>

<style lang="scss">
  @use "sass:color";
  @use "../../../../../../colors.scss" as *;
  @use "../select" as *;
  @use "../item" as *;

  .container-wrapper {
    display: flex;
  }

  .slider-top {
    display: flex;
    font-size: 12px;
  }

  .slider-right {
    margin-left: auto;
  }

  .slider-wrapper {
    flex-grow: 1;
    max-height: 35px;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }

  .delete {
    flex-shrink: 0;
    height: 35px;
    width: 35px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;

    & > img {
      width: 16px;
      height: 16px;
      opacity: 0.5;
      transition: opacity 0.3s ease;

      &:hover {
        opacity: 1;
      }
    }
  }

  .item-group-selector {
    width: 307px;
    max-height: 70px;
  }

  .container {
    gap: 10px;
    display: flex;
    width: 100%;
  }

  .slider {
    margin-top: 3px;
    padding-right: 10px;
  }
</style>
