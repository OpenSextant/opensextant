The LanguageResources project contain the geotagger pipeline in the form of a GATE application file (GAPP) and the resources that application needs.


Structure of LanguageResources Project

GAPPs - this directory contains the GATE application files (GAPPs)
	OpenSextant_Solr.gapp - the main procesing geotagger pipeline.
	OpenSextant_GeocoordsOnly.gapp - a procesing pipeline that only extracts geographic coordinates
	OpenSextant_GeneralPurpose_LITE.gapp - the geoatagging pipeline plus a small set of other basic entity types.	
	OpenSextant_GeneralPurpose.gapp - the geoatagging pipeline plus everything else we can find.		
	
resources - this directory contains the vocabularies, patterns, rules and other stuff needed by the processing pipelines
	Chunking - a start on a chunker (shallow parser) plus a bunch of other stuff
	JAPE - the rules for the geotagger, written using GATE's JAPE pattern-action rule language 
	PartOfSpeech - the lexicons and patterns for a part of speech tagger
	patterns - the regexs for geographic coordinates, date/times and other
	regex-splitter - regexs to define sentence boundaries
	SimpleEntities - some JAPE rules for some basic building block type annotations  
	tokeniser - configuration files for the tokenizer
	Vocabularies - lots of word lists, used by the rules

docs - some documentation to include the annotation model ("schema")	