<script lang="ts">
    import type {GroupItemPreference, PresetItem} from "../../../../integration/types";
    import {REST_BASE} from "../../../../integration/host";

    function getIconNameFor(item: GroupItemPreference): string {
        switch (item.group) {
            case "ARROWS":
                return "arrows";
            case "SWORD":
            case "WEAPON":
                return "weapon";
            case "AXE":
            case "HOE":
            case "SHOVEL":
            case "PICKAXE":
                return "tool";
            case "FOOD":
                return "food";
            case "POTION":
                return "potion";
            case "BLOCK":
                return "block";
            case "THROWABLE":
                return "throwable";
            default:
                throw new Error("Illegal value.");
        }
    }

    export let item: PresetItem;
</script>

{#if item.type === "SINGLE"}
    <img src="{REST_BASE}/api/v1/client/resource/itemTexture?id={item.item}" alt={item.item}/>
{:else if item.type === "GROUP"}
    <img src="img/clickgui/icon-{getIconNameFor(item)}.svg" alt="Weapons"/>
{:else if item.type === "IGNORE"}
    <img src="img/clickgui/icon-ignore.svg" alt="Tools"/>
{:else if item.type === "ANY"}
    <img src="img/clickgui/icon-any.svg" alt="Any"/>
{:else}
    <img class="muted" src="img/clickgui/icon-value-none.svg" alt="Any"/>
{/if}

<style lang="scss">
  .muted {
    fill: red !important;
  }

  img {
    width: 100%;
    height: 100%;
  }
</style>
