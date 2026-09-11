package io.lunararcdevs.lunararc.common.bridge.trading;

/** Paper-only compatibility state attached directly to the real NMS MerchantOffer. */
public interface MerchantOfferBridge {
    boolean lunararc$ignoreDiscounts();
    void lunararc$ignoreDiscounts(boolean ignoreDiscounts);
}
