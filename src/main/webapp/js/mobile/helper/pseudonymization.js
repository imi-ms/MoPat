
/**
 * This module is used to connect to the configured Pseudonymization service.
 */
function PseudonymizationService() {
}
;
/**
 * Returns the pseudonym for the given personal data.
 * 
 * @param {String} firstname Firstname of the searched pseudonym.
 * @param {String} lastname Lastname of the searched pseudonym.
 * @param {String} birthdate Birthdate of the searched pseudonym (pattern: yyyy-MM-dd).
 * 
 * @returns {String} The pseudonym for the given data.
 */
PseudonymizationService.getPseudonym = function (firstname, lastname, birthdate, clinicId) {
    var birthDate = new Date(birthdate);
    var pseudonym;
    $.ajax({
        async: false,
        url: contextPath + "/pseudonymization/pseudonym",
        type: "GET",
        contentType: "application/json; charset=utf-8",
        error: function (jqXHR, textStatus, errorThrown) {
            var requestData = "clinicId="+clinicId+"&vorname=" + firstname + "&nachname=" + lastname + "&geburtstag=" + birthDate.getDate() + "&geburtsmonat=" + birthDate.getMonth() + "&geburtsjahr=" + birthDate.getFullYear() + "&geburtsname=&plz=&ort=&sureness=true&anlegen=%2BPID%2Banfordern%2B";
            $.ajax({
                async: false,
                url: contextPath + "/pseudonymization/pseudonym",
                type: "GET",
                data: requestData,
                success: function (data) {
                    pseudonym = data.newId;
                }
            });
        },
        success: function (data) {
            var birthDay = (birthDate.getDate()).toString();
            var birthMonth = (birthDate.getMonth() + 1).toString();
            
            if(birthDay.length === 1){
                birthDay = "0" + birthDay;
            }
            
            if(birthMonth.length === 1){
                birthMonth = "0" + birthMonth;
            }
            
            var requestData = "clinicId="+clinicId+"&vorname=" + firstname + "&nachname=" + lastname + "&geburtstag=" + birthDay + "&geburtsmonat=" + birthMonth + "&geburtsjahr=" + birthDate.getFullYear() + "&geburtsname=&plz=&ort=&sureness=true&anlegen=%2BPID%2Banfordern%2B";
            $.ajax({
                async: false,
                url: data,
                type: "POST",
                data: requestData,
                error: function (jqXHR, textStatus, errorThrown) {
                    var text = textStatus;
                },
                success: function (data) {
                    pseudonym = data.newId;
                }
            });
        }
    });
    return pseudonym;
};