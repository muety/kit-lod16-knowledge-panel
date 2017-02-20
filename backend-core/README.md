# Linked Open Data Seminar 2016 - Knowledge Panel
## Backend

### Prerequisites
- Java 8 (Oracle JDK)
- Maven

### Usage
1. Place [preprocessing](https://github.com/n1try/kit-lod16-knowledge-panel/tree/master/preprocessing) output files to `data` directory
2. Adapt `config.yml` to your needs
3. `mvn assembly:assembly`
4. `mvn exec:java` (yes, I know, but packaging to JAR is not working currently)
5. Go to `http://localhost:8080/api/ranking?q=Some keyword&top=25` or `http://localhost:8080/api/infobox?q=Some keyword&top=25`

### License
MIT