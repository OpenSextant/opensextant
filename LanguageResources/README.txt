The LanguageResources project contain the geotagger pipeline in the form of a GATE application file (GAPP) and the resources that application needs.


Structure of LanguageResources Project

GAPPs - this directory contains the GATE application files (GAPPs)
	OpenSextant_Solr.gapp - the main procesing geotagger pipeline.
	OpenSextant_GeocoordsOnly.gapp - a procesing pipeline that only extracts geographic coordinates
	
resources - this directory contains the vocabularies, patterns, rules and other stuff needed by the processing pipelines
	JAPE - the rules for the geotagger, written using GATE's JAPE pattern-action rule language 
	patterns - the regexs for geographic coordinates and date/times
	regex-splitter - regexs to define sentence boundaries
	tokeniser - configuration files for the tokenizer
	Vocabularies - lots of word lists, used by the rules
	
	also included but not currently used by the geotagger
	Chunking - a start on a chunker (shallow parser) plus a bunch of other stuff
	PartOfSpeech - the lexicons and patterns for a part of speech tagger (not yet released)