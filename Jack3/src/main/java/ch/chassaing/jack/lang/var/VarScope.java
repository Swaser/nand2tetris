package ch.chassaing.jack.lang.var;

import ch.chassaing.jack.lang.Segment;
import org.jetbrains.annotations.NotNull;

public enum VarScope
{
    STATIC(Segment.STATIC),
    FIELD(Segment.THIS),
    PARAMETER(Segment.ARGUMENT),
    LOCAL(Segment.LOCAL);

    @NotNull
    public final Segment segment;

    VarScope(@NotNull Segment segment) {this.segment = segment;}
}
