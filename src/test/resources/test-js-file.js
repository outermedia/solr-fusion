importClass(java.text.SimpleDateFormat);
importClass(java.util.GregorianCalendar);
var now = new GregorianCalendar(2014, 6, 19);
var fmt = new SimpleDateFormat("yyyy-MM-dd");
// predefined variables: see ScriptEnv.ENV_*
print("Conversion         : "+conversion+"\n");
print("Values             : "+values+"\n");
print("Search Server Field: "+searchServerField+"\n");
print("Search Server Value: "+searchServerValue+"\n");
print("Fusion Field       : "+fusionField+"\n");
print("Fusion Value       : "+fusionValue+"\n");
print("Fusion Field       : "+fusionFieldDeclaration+"\n");
print("Fusion Schema      : "+fusionSchema+"\n");
returnValues = searchServerValue.get(0)+" at "+fmt.format(now.getTime().getTime());