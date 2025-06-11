# Manual to AuroraBackend

The class AuroraBackend has three fields and several methods.

## Fields

### `url`

The field `url` is a string to the NOAA product for its three day aurora forecast.

### `info`

The field `info` is a string that contains the downloaded information from `url`.
It will be used and manipulated in various subsequent methods. This field is private. Use the getter `getinfo()`.

### `tonight`

The field `tonight` is a string that contains what hours tonight 1800-0600 that the aurora is active.
It is probably the field you should put into the notification body. This field is private. Use the getter `gettonight()`

## Methods

### `getinfo()`

Ths method is a getter for `info`. Useful for debugging `parseinfo()`.

### `gettonight()`

This method is a getter for `tonight`. You should use this if you want to know `tonight`.

### `updateinfo()`

This method downloads (pulls) the information from `url` into `info`. It returns `void`.

### `parseinfo()`

This method parses `info` into something more useful for later usage. This modifies `info` permanently. It returns `void`.

### `getRow(ZonedDateTime datetime)`

This method returns the row of the hour in the given `datetime`. This method is private.

### `getCol(ZonedDateTime datetime)`

This method returns the column of the date in the given `datetime`. This method is private.

### `getKp(ZonedDateTime datetime)`

This method returns the aurora Kp value of the given `datetime`. Returns `-1` if `datetime` or `info` is insufficient.

### `getGScale(double kp)`

This method returns a G Scale number for the Kp value `kp`.

### `activeOn(ZonedDateTime datetime)`

This method returns a string on how active an aurora is on the given `datetime`.

### `activeTonight(ZonedDateTime datetime)`

This method modifies `tonight` so that it will contain all the hours that the aurora will be active. It returns `void`.

## Usage

The following pseudocode should be run after 0030hrs UTC and 1230hrs UTC.

One possible usage in pseudocode:

    AuroraBackend = new AuroraBackend();
    if (isInternetGood() == true) {
        // make sure to check for internet connection before executing next line
        aurora.updateinfo(); // note this prints an exception if it fails and info remains as null.
    }
    else {
        waitforInternet();
    }
    if (aurora.getinfo() == null) {
        // implement the case if info for some reason cannot be downloaded. Stop advancing out of this if block.
    }
    // here it assumes aurora.info has some useful information about the aurora activity.
    aurora.parseinfo();
    ZonedDateTime when = ZonedDateTime.now();
    aurora.activeTonight(when);
    if (aurora.gettonight() != null) {
        // make a notification with aurora.gettonight()
    }

