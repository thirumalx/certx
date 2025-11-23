package io.github.thirumalx.model;

public sealed interface Attribute<T> permits HistorizedAttribute, SimpleAttribute{
    
    public static final Long METADATA_ACTIVE = 1L;

    Long getAnchorId();
    T getValue();
    Long getMetadataId();

}
