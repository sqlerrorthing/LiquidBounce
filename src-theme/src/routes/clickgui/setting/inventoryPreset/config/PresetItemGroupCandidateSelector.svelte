<script lang="ts">
    import type {PresetItem} from "../../../../../integration/types";
    import ItemGroupSelector from "./ItemGroupSelector.svelte";
    import {createEventDispatcher} from "svelte";
    import PresetTooltip from "./PresetTooltip.svelte";

    export let items: PresetItem[];
    export let parentExpanded: boolean

    const dispatch = createEventDispatcher();

    function handleChange() {
        dispatch("change")
    }
</script>

<div class="wrapper">
    <div class="select-title">
        <span>Candidates <PresetTooltip align="bottom_center" text="All possible items that will be in this slot, the first item, the higher its importance, if there is no item in the slot, the lower one will be selected in importance"/></span>
    </div>
    <div class="candidates">
        <ItemGroupSelector
                canSelectAny={false}
                canSelectIgnore={true}
                bind:parentExpanded
                bind:items={items}
                on:change={handleChange}
                style="width: 100%px; max-height: 100%"
        />
    </div>
</div>

<style lang="scss">
  @use "sass:color";
  @use "../../../../../colors.scss" as *;
  @use "select" as *;
  @use "item" as *;

  .candidates {
    margin-top: 20px;
    height: calc(100% - 40px);
  }

  .wrapper {
    position: relative;
    height: 100%;
  }
</style>
