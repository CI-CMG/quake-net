/************************************************************************************************************************************/
//File: parseQuakeML.groovy
//Purpose: Parse data from QuakeML files and output requested information to .txt file.
//Author: Austin Curtis
//Oder of Operations:
//	1.Slurp in file and ge the required data at specified fields.
//	2.Find the corect magnitude (Mww) and set that value to fmag as well as ftype.
//	3.Check for information in Felt/Damage section.
//	4.Parse date into the correct format.
//	5.Convert Depth from m to km
//	6.Write out to three .txt files one with Felt/Damage info (20**_**_FD.txt),  one without (20**_**.txt) and one with evernts where mag>=7.5 or has Felt/Damage info that matches keywords.
/************************************************************************************************************************************/
import java.util.regex.*

if (args.size() == 0) {
	println "Usage: parseQuakeML <input file>"
	return
}
def inFile = new File(args[0])
assert inFile.exists()

/************************************************************************************************************************************/
//wordsInString Function
//Purpose: Search a string for key words.
// Input: words (List of words you are looking for) and str (The string you want to search)
//Output: none but acts as boolean.

def wordsInString(words, str) {
	def strWords = str.toLowerCase().split(/\s+/)
	words.findAll { it.toLowerCase() in strWords }
}
def keyWords = [/damage/, /destroy/, /dollar/, /building/, /million/, /house/, /landslide/, /rockslide/, /outage/, /village/, /town/, /bridge/, /road/, /homeless/,
				/utilities/, /tsunami/, /seiche/, /washed/, /city/, /cities/, /death/, /casualt/, /injur/, /kill/, /die/, /livestock/, /heart attack/]


/************************************************************************************************************************************/
//Slurp in file and ge the required data at specified fields.

def slurper = new XmlSlurper()
def xml = slurper.parse(inFile)

def text = xml.eventParameters.event.comment.text
def mag = xml.eventParameters.event.magnitude.mag.value
def magt= xml.eventParameters.event.magnitude.type
def origin = xml.eventParameters.event.origin[0]
def dateTime = xml.eventParameters.event.origin.time.value
def longitude = origin.longitude.value
def latitude = origin.latitude.value
def depth = origin.depth.value
def where = xml.eventParameters.event.description.text[0]

/************************************************************************************************************************************/
//Mag checkpoint
//If there are multiple Mag's then check for Mww and at the same time Check for any Mw* Mag's.  If there are Mw*'s put there list location into list "list".
//Check the MW's  to see if they match any of the specific Mw's in order from most desireable to least.  If a mag matches write the values to fmag and ftype and exit loop.
//If there are no Mw's check for the next most desireable mag types.
//If there is only one mag, set famg and ftype

//println mag

def fmag=0
def ftype=""
if(mag.size() > 1){
	def tsz = magt.size()
	//println magt.size()
	def list = []
	for(i=0;i<tsz;i++){
		if (magt[i].text() =~ /\bMw\w*\b/) list << i
		//println list
		if(magt[i].text() == "Mww"){
			fmag=mag[i]
			ftype=magt[i].text()
			break;
		}
	}
	def lsz = list.size()
	if(fmag != 0){

	}else if (fmag == 0 && list){
		for(i=0;i<lsz;i++){
			if(magt[list[i]].text() == "Mwc"){
				fmag=mag[list[i]]
				ftype=magt[list[i]].text()
				break;
			}
		}
		if (fmag == 0){
			for(i=0;i<lsz;i++){
				if(magt[list[i]].text() == "Mwb"){
					fmag=mag[list[i]]
					ftype=magt[list[i]].text()
					break;
				}
			}
			if (fmag == 0){
				for(i=0;i<lsz;i++){
					if(magt[list[i]].text() == "Mwr"){
						fmag=mag[list[i]]
						ftype=magt[list[i]].text()
						break;
					}
				}
				if (fmag == 0){
					for(i=0;i<lsz;i++){
						if(magt[list[i]].text() == "Mw"){
							fmag=mag[list[i]]
							ftype=magt[list[i]].text()
							break;
						}
					}
					if (fmag == 0){
						for(i=0;i<lsz;i++){
							if(magt[list[i]].text() == "Mwp"){
								fmag=mag[list[i]]
								ftype=magt[list[i]].text()
								break;
							}
						}
						if (fmag == 0) {
							fmag = mag[list[0]]
							ftype=magt[list[0]].text()
						}
					}
				}
			}
		}
	}else{
		for(i=0;i<tsz;i++){
			if(magt[i].text() == "Mi"){
				fmag=mag[i]
				ftype=magt[i].text()
				break;
			}
		}
		if (fmag == 0){
			for(i=0;i<tsz;i++){
				if(magt[i].text() == "Ms_20"){
					fmag=mag[i]
					ftype=magt[i].text()
					break;
				}
			}
			for(i=0;i<tsz;i++){
				if(magt[i].text() == "mb"){
					fmag=mag[i]
					ftype=magt[i].text()
					break;
				}
			}
			for(i=0;i<tsz;i++){
				if(magt[i].text() == "ML"){
					fmag=mag[i]
					ftype=magt[i].text()
					break;
				}
			}
		}
	}

}else{
	fmag=mag
	ftype=magt.text()
}


/************************************************************************************************************************************/
//text checkpoint

if(text == "") text = "N/A"

/************************************************************************************************************************************/
//Date checkpoint


def date=0
try { 
	//Protected code
	date = Date.parse("yyyy-MM-dd'T'HH:mm:SS.ss'Z'", dateTime.text()).toCalendar() 
} catch (java.text.ParseException e){
   	//Catch block 
	date = Date.parse("yyyy-MM-dd'T'HH:mm:SS'Z'", dateTime.text()).toCalendar()
//	println "Caught"
}

/************************************************************************************************************************************/
//Depth Conversion
// Converts obj to Integer and then casts as a double. Divide by 1000 to convert to km.
double dep = depth[0].toDouble()
dep = dep/1000
//println dep
//

/************************************************************************************************************************************/
//write FD file
// Writes to file year_month_FD.txt. If none exists then create one with column names at top.


def outFile = new File("${date.get(Calendar.YEAR)}_${date.get(Calendar.MONTH)+1}_FD.txt")
if(outFile.exists()){
outFile.append("\n\n${date.get(Calendar.YEAR)} \t ${date.get(Calendar.MONTH)+1} \t ${date.get(Calendar.DATE)} \t ${date.get(Calendar.HOUR_OF_DAY)} \t ${date.get(Calendar.MINUTE)} \t ${date.get(Calendar.SECOND)} \t ${dep} \t ${latitude} \t ${longitude} \t ${fmag} \t ${ftype} \t ${where}");
outFile.append("\nfelt/damage: ")
outFile.append("${text}\n")
}else{
new File("${date.get(Calendar.YEAR)}_${date.get(Calendar.MONTH)+1}_FD.txt").withWriter('utf-8'){
writer -> writer.writeLine("year\tmonth\tday\thour\tminute\tsecond\tdepth\tlatitude\tlongitude\tmagnitude\tType\tLocation");
}
outFile.append("\n\n${date.get(Calendar.YEAR)} \t ${date.get(Calendar.MONTH)+1} \t ${date.get(Calendar.DATE)} \t ${date.get(Calendar.HOUR_OF_DAY)} \t ${date.get(Calendar.MINUTE)} \t ${date.get(Calendar.SECOND)} \t ${dep} \t ${latitude} \t ${longitude} \t ${fmag} \t ${ftype} \t ${where}");
outFile.append("\nfelt/damage: ")
outFile.append("${text}\n")
}

/************************************************************************************************************************************/
//write simple file
//Writes to file year_month.txt. If none exists then create one with column names at top.

def sFile = new File("${date.get(Calendar.YEAR)}_${date.get(Calendar.MONTH)+1}.txt")
if(sFile.exists()){
sFile.append("\n${date.get(Calendar.YEAR)} \t ${date.get(Calendar.MONTH)+1} \t ${date.get(Calendar.DATE)} \t ${date.get(Calendar.HOUR_OF_DAY)} \t ${date.get(Calendar.MINUTE)} \t ${date.get(Calendar.SECOND)} \t ${dep} \t ${latitude} \t ${longitude} \t ${fmag} \t ${ftype} \t ${where}");
}else{
new File("${date.get(Calendar.YEAR)}_${date.get(Calendar.MONTH)+1}.txt").withWriter('utf-8'){
writer -> writer.writeLine("year\tmonth\tday\thour\tminute\tsecond\tdepth\tlatitude\tlongitude\tmagnitude\tType\tLocation");
}
sFile.append("\n${date.get(Calendar.YEAR)} \t ${date.get(Calendar.MONTH)+1} \t ${date.get(Calendar.DATE)} \t ${date.get(Calendar.HOUR_OF_DAY)} \t ${date.get(Calendar.MINUTE)} \t ${date.get(Calendar.SECOND)} \t ${dep} \t ${latitude} \t ${longitude} \t ${fmag} \t ${ftype} \t ${where}");
}

/************************************************************************************************************************************/
//write BIG file
//Writes to file year_month_BIG.txt if mag >=7.5 or the Felt/Damage section matches any of our keywords. If none exists then create one with column names at top.

double ch = 7.5
fmag = fmag.toDouble()

if (wordsInString(keyWords, text.toString()) || fmag >= ch){
	def tFile = new File("${date.get(Calendar.YEAR)}_${date.get(Calendar.MONTH)+1}_BIG.txt")
	if(tFile.exists()){
		tFile.append("\n\n${date.get(Calendar.YEAR)} \t ${date.get(Calendar.MONTH)+1} \t ${date.get(Calendar.DATE)} \t ${date.get(Calendar.HOUR_OF_DAY)} \t ${date.get(Calendar.MINUTE)} \t ${date.get(Calendar.SECOND)} \t ${dep} \t ${latitude} \t ${longitude} \t ${fmag} \t ${ftype} \t ${where}");
		tFile.append("\nfelt/damage: ")
		tFile.append("${text}\n")
	}else{
		new File("${date.get(Calendar.YEAR)}_${date.get(Calendar.MONTH)+1}_BIG.txt").withWriter('utf-8'){
			writer -> writer.writeLine("year\tmonth\tday\thour\tminute\tsecond\tdepth\tlatitude\tlongitude\tmagnitude\tType\tLocation");
		}
		tFile.append("\n\n${date.get(Calendar.YEAR)} \t ${date.get(Calendar.MONTH)+1} \t ${date.get(Calendar.DATE)} \t ${date.get(Calendar.HOUR_OF_DAY)} \t ${date.get(Calendar.MINUTE)} \t ${date.get(Calendar.SECOND)} \t ${dep} \t ${latitude} \t ${longitude} \t ${fmag} \t ${ftype} \t ${where}");
		tFile.append("\nfelt/damage: ")
		tFile.append("${text}\n")
	}
}

/************************************************************************************************************************************/
//Print statements
// Comented print statements are left for debugging purposes and last two prints signal completion

//println "year\tmonth\tday\thour\tminute\tsecond\tdepth\tlatitude\tlongitude\tmagnitude\tfelt/damage"
//println "${date.get(Calendar.YEAR)}\t${date.get(Calendar.MONTH)+1}\t${date.get(Calendar.DATE)}\t${date.get(Calendar.HOUR)}\t${date.get(Calendar.MINUTE)}\t${date.get(Calendar.SECOND)}\t${depth}\t${latitude}\t${longitude}\t${fmag}\t${text}"
println inFile
println "Done!"
