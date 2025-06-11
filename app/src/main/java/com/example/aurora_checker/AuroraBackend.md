# Manual to AuroraBackend

The class AuroraBackend has two fields and several methods.

# Fields

## `url`

The field `url` is a string to the NOAA product for its three day aurora forecast.

## `info`

The field `info` is a string that contains the downloaded information from `url`.
It will be used and manipulated in various subsequent methods.

## `tonight`

The field `tonight` is a string that contains what hours tonight 1800-0600 that the aurora is active.
It is probably the field you should put into the notification body.

# Methods

## `getinfo()`

Ths method is a getter for `info`. Useful for debugging `parseinfo()`.

## `gettonight()`

This method is a getter for `tonight`. You should use this if you want to know `tonight`.

## `updateinfo()`

This method downloads (pulls) the information from `url` into `info`.

## `parseinfo()`

This method parses `info` into something more useful for later usage. This modifies `info` permanently.

## `getRow(ZonedDateTime datetime)`

This method returns the row of the hour in the given `datetime`. This method is private.

## `getCol(ZonedDateTime datetime)`

This method returns the column of the date in the given `datetime`. This method is private.

## `getKp(ZonedDateTime datetime)`

This method returns the aurora Kp value of the given `datetime`. Returns `-1` if `datetime` or `info` is insufficient.

## `getGScale(double kp)`

This method returns a G Scale number for the Kp value `kp`.

## `activeOn(ZonedDateTime datetime)`

This method returns a string on how active an aurora is on the given `datetime`.

## `activeTonight(ZonedDateTime datetime)`

This method modifies `tonight` so that it will contain all the hours that the aurora will be active on.
