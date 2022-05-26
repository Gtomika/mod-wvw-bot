package com.gaspar.modwvwbot.misc;

import com.gaspar.modwvwbot.model.Amount;
import com.gaspar.modwvwbot.model.WvwItemOrCurrency;
import com.gaspar.modwvwbot.model.gw2api.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Methods that count and summarize items in
 * inventories, bank, material storage and wallet.
 */
public abstract class AmountUtils {

    /**
     * Create a list of amounts with 0 count (each item or currency has 0 amount).
     * @param itemsOrCurrencies Items or currencies.
     * @return 0 amount list.
     */
    public static List<Amount> emptyAmounts(List<WvwItemOrCurrency> itemsOrCurrencies) {
        return itemsOrCurrencies.stream()
                .map(ioc -> new Amount(ioc, 0))
                .collect(Collectors.toList());
    }

    /*
    /**
     * Add some new item or currency amounts to an existing list of them.
     * @param amounts Original amounts that are to be increased.
     * @param newAmounts New amounts that are to be added to {@code amounts}. If this has some items that
     *                   are not in the original list, those will be lost.
    public static void summarize(List<Amount> amounts, List<Amount> newAmounts) {
        if(newAmounts.isEmpty()) return;
        for(Amount newAmount: newAmounts) {
            for(Amount amount: amounts) {
                if(amount.sameItem(newAmount)) {
                    amount.increaseAmountBy(newAmount.getAmount());
                    break;
                }
            }
        }
    }
    */

    /**
     * Count items in an inventory.
     * @param amounts Amount list that stores how many items there are.
     * @param inventory The inventory contents.
     */
    public static void countInInventory(List<Amount> amounts, InventoryResponse inventory) {
        for(BagResponse bag: inventory.getBags()) {
            if(bag == null) { //happens when that character has nothing equipped in that bag slot
                continue;
            }
            countItemList(amounts, bag.getItems());
        }
    }

    /**
     * Count items of interest in a list of items.
     * @param amounts Amount of items. This list will be modified.
     * @param items All items found. Searched for items of interest.
     */
    public static void countItemList(List<Amount> amounts, List<ItemResponse> items) {
        for(ItemResponse item: items) {
            //null item means there is nothing in that inventory slot
            if(item == null) continue;
            //is this item one of the items of interest?
            for(var amount: amounts) {
                if(item.getId() == amount.getItemOrCurrency().getId()) {
                    amount.increaseAmountBy(item.getAmount());
                    break;
                }
            }
        }
    }

    /**
     * Count items of interest in an array of items.
     * @param amounts Amount of items. This list will be modified.
     * @param items All items found. Searched for items of interest.
     */
    public static void countItemArray(List<Amount> amounts, ItemResponse[] items) {
        for(ItemResponse item: items) {
            //null item means there is nothing in that inventory slot
            if(item == null) continue;
            //is this item one of the items of interest?
            for(var amount: amounts) {
                if(item.getId() == amount.getItemOrCurrency().getId()) {
                    amount.increaseAmountBy(item.getAmount());
                    break;
                }
            }
        }
    }

    /**
     * Count currencies of interest in an array of currencies.
     * @param amounts Amount of currencies. This list will be modified.
     * @param currencies All currencies found. Searched for currencies of interest.
     */
    public static void countCurrencyArray(List<Amount> amounts, CurrencyResponse[] currencies) {
        for(CurrencyResponse currency: currencies) {
            if(currency == null) continue;
            //is this a currency of interest?
            for(var amount: amounts) {
                if(currency.getId() == amount.getItemOrCurrency().getId()) {
                    amount.increaseAmountBy(currency.getAmount());
                    break;
                }
            }
        }
    }

}
