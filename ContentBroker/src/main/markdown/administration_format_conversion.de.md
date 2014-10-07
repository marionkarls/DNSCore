	/*
	  DA-NRW Software Suite | ContentBroker
	  Copyright (C) 2014 LVRInfoKom
	  Landschaftsverband Rheinland
	
	  This program is free software: you can redistribute it and/or modify
	  it under the terms of the GNU General Public License as published by
	  the Free Software Foundation, either version 3 of the License, or
	  (at your option) any later version.
	
	  This program is distributed in the hope that it will be useful,
	  but WITHOUT ANY WARRANTY; without even the implied warranty of
	  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	  GNU General Public License for more details.
	
	  You should have received a copy of the GNU General Public License
	  along with this program.  If not, see <http://www.gnu.org/licenses/>.
	*/

# Formatkonversion mit DNSCore

Formatkonversionen in DNSCore basieren auf einem Modell von [Konversionsrichtlinien](object_model.de.md#conversionpolicy---die-regel-zur-anwendung-einer-konversion) (ConversionPolicies) und [Konversionsroutinen](object_model.de.md#conversionroutine---die-konversionsroutine). Konversionsroutinen beschreiben ein Verfahren, mit dessen Hilfe eine Datei eines bestimmten Formates in ein anderes Zielformat konvertiert werden kann. Konversionsrichtlinien hingegen legen fest, welche Konversionsroutinen für Dateien mit bestimmten Dateiformaten durchzuführen sind, nachdem ebendiese Dateiformate vom System erkannt wurden.

![Bild](https://raw.githubusercontent.com/da-nrw/DNSCore/master/ContentBroker/src/main/markdown/object_model_object_users.jpg)

Sowohl **Konversionsrichtlinien** als auch **Konversionsroutinen** sind Eigenschaften des **Gesamtsystems**. Wenn eine Konversionsroutine im System angemeldet wird, so muss sichergestellt werden, dass alle **Knoten** des Systems diese unterstützen. 
(TODO was heisst das , Konverter vs. Java-Konversionen)

## Anlegen und Testen von neuen Konversionsrichtlinien und Routinen

TODO CLI wird hier beschrieben.

**1 - Evaluationsphase** Um eine neue Konversionsroutine systemweit bereitstellen zu können, muss diese zunächst im Vorgeld evaluiert werden. Die Grundvoraussetzung lautet hier, dass das Programm mithilfe eines simplen Unix-Kommandozeilenaufrufes aufgerufen werden kann. Der Kommandozeilenaufruf sollte dann lokal getestet werden.

**2 - Installationsphase ** Als nächstes müssen alle Administratoren des Systems auf den jeweils von ihnen betreuten Knoten die entsprechend benötigten Konverter in der spezifizierten Version eingerichtet werden. 

**3 - Anmeldungsphase** Dann müssen die Einträge in der Datenbank eingerichtet werden. Dies geschieht "manuell". Die Beschreibungen der Tabllenstruktur ist weiter [unten](#einrichten--db) beschrieben. Es der Eintrag für die Konversionsroutine und verschiedene Einträge für die Konversionrichtlinien hinzugefügt werden.

**4 - Konfigurationstestphase** 


## Einrichten / DB

Für die Policies ist die Tabelle "conversion_policies" eingerichtet.

    source_format: varchar
    conversion_routines_id: int
    presentation: boolean
    
**source_format** Erklärung

**conversion_routines_id** Erklärung

**presentation** Erklärung

Für die Routinen ist die Tabelle

    name
    type
    target_suffix
    params
    
**name** Ein beliebig zur wählender Name für die Konversionsroutine. Er kann so gewählt werden, das er z.B. Aufschluss über die eingesetzten Konverter liefert.

**type** Hier ist ein vollqualifizierter Name einer Java-Klasse einzusetzen. Die zur Verfügung stehenden Typen sind in einem folgenden [Abschnitt](administration_format_conversion.de.md#typen-von-konversionsroutinen) beschrieben.

**params** Optionaler Parameter für kommandozeilenbasierte Konversionsroutinentypen (type=CLIConversionStrategy bzw. PublishCLIConversionStrategy). Kann bei anderen Konversionsroutinentypen leer bleiben. 

"params" spezifiziert den Kommandozeilenaufruf inklusive der Platzhalter für die Ein- und Ausgangsdatei. Im einfachsten Falle kann dies wie folgt aussehen

    convert input output

Der ContentBroker würde die Platzhalter durch die entsprechenden Pfade der Ein- und Ausgangsdateien ersetzen und den Befehl convert (Bestandteil von ImageMagick) auf der Kommandozeile absetzen.

**target_suffix** Optionaler Parameter für kommandozeilenbasierte Konversionsroutinentypen (type=CLIConversionStrategy bzw. PublishCLIConversionStrategy). Kann für die übrigen Konversionsroutinentypen leer bleiben.


## Typen von Konversionsroutinen

**de.uzk.hki.da.format.CLIConversionStrategy.java**

Setzt einen beliebigen Befehl auf der Kommandozeile ab und kann somit jegliche von dort aufrufbare Converter einbinden. Benötigt entsprechende Werte für "params" und "target_suffix".

**de.uzk.hki.da.format.PublishImageConversionStrategy.java**
 
**de.uzk.hki.da.format.PublishAudioConversionStrategy.java**

**de.uzk.hki.da.format.PublishVideoConversionStrategy.java**

**de.uzk.hki.da.format.PublishImageConversionStrategy.java**

**de.uzk.hki.da.format.PublishPDFConversionStrategy.java**

## Workflow des Systems zur Formatkonversion.

![Formatkonversionsworkflow](https://raw.githubusercontent.com/da-nrw/DNSCore/master/ContentBroker/src/main/markdown/format_conversion_workflow.jpg)

Ingest
Migration
PIPGenerierung

## Funktionsweise

![ConversionInstructions](https://raw.githubusercontent.com/da-nrw/DNSCore/master/ContentBroker/src/main/markdown/object_model_conversion_dafiles.jpg)

Generierung der Konversionsinstruktionen


