
Currently there are three OpenSextant extraction pipelines, each defined in its own GATE application (GAPP) file:
1) The OpenSextant Geotagger (defined in OpenSextant_Solr.gapp)
2) The OpenSextant General Purpose Extractor LITE (defined in OpenSextant_GeneralPurpose_LITE.gapp)
3) The OpenSextant General Purpose Extractor FULL (defined in OpenSextant_GeneralPurpose.gapp)


The OpenSextant Entities spreadsheet (OpenSextant Entities.xlsx) defines the GATE annotations (types,features and feature values)produced by these OpenSextant processes.
Annotation Model

Every OpenSextant annotation has the following standard features: 
	"string" - the text as found in the document (control characters and excess whitespace removed)
	"hierarchy" - the taxonomic categorization of the entity type 
In addition to these standard features, annotations may have type specific features (see below and spreadsheet for details).

The OpenSextant Entities spreadsheet  contains the following information:
	EntityType - the type of entity denoted by this annotation. This is the type of the GATE Annotation or the value of the "EntityType" feature if using the flattened form (see below for details of the flattened form)
	hierarchy - the type of entity as a hierarchical (taxonomic) categorization. This is consistent with but usually more specific than the EntityType label.
	Type specific features - the features names and value types which are specific to that annotation type. Written as <FEATURE_NAME>(<VALUE_TYPE>)
	Geotagger - if produced by the OpenSextant Geotagger process
	GeneralPurpose Lite - if produced by the OpenSextant General Purpose LITE process 
	General Purpose Full	Source - if produced by the Full OpenSextant General Purpose process 
	Source - the internal OpenSextant component which generates this type (info only)

Flattened entities
The flattened form of the entity model is a convenience for simple clients who would prefer to not deal with a unbounded list of annotation types. This flatten form converts the (evergrowing) list of entities into a single annotation type of type "Entity". It adds a "EntityType" feature. All of the standard and type specific features are identical to the unflattened model described above. 


Note that OpenSextant creates numerous annotations besides those described in the spreadsheet. Any annotation other than those listed in the OpenSextant Entities.xlsx spreadsheet should be considered internal content. They may represent alternative interpretations, pre-disambiguated values or "building block" annotations which should not be used outside of a specific context. Use at your own risk.   
