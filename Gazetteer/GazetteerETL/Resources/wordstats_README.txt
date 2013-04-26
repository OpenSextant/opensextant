
The "wordstats.txt" file contains basic counts,statistics and derived confidences about words/phrases (n-grams), mostly single (1 token) words/phrases. It is used in the gazetteer processing to help estimate a prior confidence (i.e. a confidence value calculated prior to seeing this place name in a document) that a place name represents a place and not some other category (like a person, organization ...).

The wordstats file currently contains the following fields:  
Ngram - the word/phrase in all-lower case with a single space between tokens if multi-token
Upper - the count of how many times the Ngram appeared in the corpus as all-upper case (ABCD)
Lower - the count of how many times the Ngram appeared in the corpus as all-lower case (abcd)
Initial - the count of how many times the Ngram appeared in the corpus as initial letter upper case (Abcd)
Mixed - the count of how many times the Ngram appeared in the corpus in a case form not covered by one of the above (aBcd)
Total - the count of how many times the Ngram appeared in the corpus (in any case form)
Decision - the decision of the majority case form ( most often occurring case form): [upper,lower,initial,mixed] 
CaseConfidence - the confidence of the case decision (maximum count/total count). Value 0.0->1.0
ProperConfidence - the confidence that this could be a proper noun (  (inital count+mixed count)/total count). Value 0.0->1.0
AbbreviationConfidence - the confidence that this could be a proper noun (  (upper count)/total count). Value 0.0->1.0

 
The data contained in the wordstats file is derived from the Google Books Ngram data set (see http://books.google.com/ngrams for details), specifically the 1-grams of the "English" collection (20090715 version) available at  http://storage.googleapis.com/books/ngrams/books/datasetsv2.html.
The Google Books Ngram dataset is licensed by Google Inc. under a Creative Commons Attribution 3.0 Unported License. 
Thanks to Google for making this data set available. 

