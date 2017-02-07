# Linked Open Data Seminar 2016 - Knowledge Panel

### Description
This is a project in the context of the _Linked Open Data_ (LOD) Seminar at [AIFB](http://aifb.kit.edu) at the [Karlsruhe Institute of Technology](http://kit.edu). 
Goal was basically to integrate multiple LOD sources (in a first step only [DBPedia](http://dbpedia.org) and [Yago](http://yago-knowledge.org)) to build a knowledge pabel or fact box (as known from Google or Wikipedia) on that basis. 
A major challange was how to determine which properties of an entity, e.g. [dbp:Karlsruhe](https://dbpedia.org/resource/Karlsruhe) are relevant and meaningful to be displayed to the user and which are not. Accordingly, a ranking of properties for specific entities or classes (`rdf:type`) of entities had to be elaborated, which is capable of ranking properties among multiple, distinct sources.
While [[1](http://doi.org/10.1016/j.future.2015.04.018)] already presented a good solution (although only working for one dataset, namely DBPedia) based on supervised machine learning, our approach is based of rather naive statistical metrics like [TD-IDF](https://en.wikipedia.org/wiki/Tf%E2%80%93idf).
Our evaluation is based on _rank biased overlap_ (RBO), as described in [[2](http://doi.org/10.1145/1852102.1852106)].

[1] Dessi, A., & Atzori, M. (2016). A machine-learning approach to ranking RDF properties. Future Generation Computer Systems, 54, 366–377. http://doi.org/10.1016/j.future.2015.04.018

[2] Webber, W., Moffat, A., & Zobel, J. (2010). A similarity measure for indefinite rankings. ACM Transactions on Information Systems, 28(4), 1–38. http://doi.org/10.1145/1852102.1852106

### Implementation
The project consist of four software components.
* __Preprocessing scripts__: Responsible for extracting statistics from LOD graphs and calculating TF and IDF on that base
* __Backend__: Responsible for computing entity-specific, multi-source property ranking at runtime as well as constructing a combined JSON-LD serialized RDF graph from DBPedia and Yago on that base. Exposed as a RESTful webservice.
* __Frontend__: Single Page App as user interface, which queries the backend based in a user input and prints a knowledge panel based on the response's RDF graph.
* __Evaluation__: Scripts facilitating "manual" computation of RBO metrics for specific entities.

#### UML component diagram
![](http://i.imgur.com/XtUNg1Y.jpg)

#### UML sequence diagram
![](http://i.imgur.com/fdJWLaX.jpg)

### Team
- Han Che
- Benny Rolle
- [Ferdinand Mütsch](https://ferdinand-muetsch.de)
### License
MIT