# Mask Reminder Aplication

Google Play Link:
https://play.google.com/store/apps/details?id=com.apricot.maskreminder

## How My Location Service Works? ##
 /*****************************************************************************
 * Since using GPS may cause significant amount of power usage in long term,
 * I decided not to force the user to use it. But it is highly possible 
 * for the other location data sources like wifi or cellular data to be incorrect.
 * To solve this problem, I have written the algorithm below.
 *
 * ITS WORKING METHOD IS LIKE THIS:
 *
 * AFTER THE SERVICE GETS THE FIRST INFO OF 'THE USER OUTSIDE',
 * IT DOES NOT DIRECTLY NOTIFY THE USER. BEFORE THAT, IT CHECKS THE LOCATION
 * 5 TIMES IN A ROW TO MAKE SURE THIS INFO IS CORRECT
 * AND IF ALL THE 5 DATA INDICATES OUTSIDE,
 * SERVICE SHOWS THE NOTIFICATION, EVENTUALLY.
 *
 * TO MAKE THIS PROCESS FASTER, THE SERVICE CREATES A NEW "FAST LOCATION
 * REQUESTER" AFTER GETTING THE FIRST 'OUTSIDE' INFO AND
 * USES THIS REQUESTER TO FINISH 5-STEP VERIFICATION...
 *
 * THE REASON OF USING FAST REQUESTER IS THE THAT THE NORMAL REQUESTER SO SLOW
 * FOR CHECKING THE LOCATION FOR 5 TIME...  (7 SECONDS PER REQUEST)
 *
 * AFTER FINISHING THE VERIFICATION, IT RETURNS TO THE NORMAL REQUESTER...
 *
 ******************************************************************************/
   
   
