# membrane-re-frame-example

A proof of concept to demonstrate using re-frame for desktop and terminal apps.

The code is largely derived from https://github.com/day8/re-frame/tree/master/examples/todomvc. Except for some browser specific code, the subs, db, and events are the same.

Both the desktop and the terminal app fully share subs, events, and db. Each has their own view.

## Screenshots

### Desktop
![desktop](desktop-demo.gif?raw=true)

### Terminal
![terminal example](term-demo.gif?raw=true)


## Usage

### Desktop
`$ lein run -m membrane-re-frame-example.views`
### Terminal
`$ lein run -m membrane-re-frame-example.term-view`

## License

Copyright © 2020 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
