package com.agoramp.minecraft.util.api.text.transform;

import net.kyori.adventure.text.event.ClickEvent;

public class LinkTransform extends ClickTransform {
    private LinkTransform() {
        super();
    }

    public LinkTransform(String link) {
        super(ClickEvent.openUrl(link));
    }
}
