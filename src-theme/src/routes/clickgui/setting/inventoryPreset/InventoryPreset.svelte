<script lang="ts">
    import type {InventoryPreset} from "../../../../integration/types";
    import PresetModal from "./config/PresetModal.svelte";
    import {createEventDispatcher} from "svelte";
    import ItemPreview from "./ItemPreview.svelte";

    export let preset: InventoryPreset

    const dispatch = createEventDispatcher();

    function handleChange() {
        dispatch("change")
    }

    let configuring = false
</script>

<!-- svelte-ignore a11y-click-events-have-key-events -->
<!-- svelte-ignore a11y-no-static-element-interactions -->
<div class="preset" on:click={() => configuring = true}>
    {#each preset.items as group, idx (idx)}
        <ItemPreview bind:group />
    {/each}
</div>

{#if configuring}
    <PresetModal
            bind:preset
            on:close={() => configuring = false}
            on:change={handleChange}
    />
{/if}

<style lang="scss">
  .preset {
    display: flex;
    justify-content: space-between;
    cursor: pointer;
  }
</style>
