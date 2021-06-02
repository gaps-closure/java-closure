# CLOSURE Workflow for Java


## Annotation

* The CDS developer using CLOSURE needs to know `com.peratonlabs.closure.cle.Cledef` as well as the CLE-JSON schema
* Developer creates a copy of the application for CLOSURE annotations
* Developer defines a number of CLE annotations relevant for the application, which are in turn are annotated using Cledef, which will associate a CLE-JSON with that annotation
* Developer applies annotations to application source to capture cross-domain partitioning intent

## Conflict Analyzer

* Developer runs conflict analyzer on annotated application source code
* Output of conflict analyzer is one of:
  - failure, with description of conflict plus developer refactoring guidance to mitigate conflict
  - success, with a output `topology.json`
     -- every application class is assigned to a unique enclave or to a library common to all enclaves
     -- identifies methods and constructors that are in the cross-domain cut

## GEDL Program Analyzer

* For each method in the cut, for each parameter infer direction and (collection) size; also infer structure of the object via reflection
  - output .GEDL file contains the method signatures and inferred information for each method in the cut
 
## Autogeneration and Refactoring

* Replace method and constructor calls with RPC using aspects
* Replace local method invocation with RPC
* Generate main plus code for RPC handling on remote side
* Generate code for un/marshalling and serialization which will be invoked by the RPC call and handler
* DFDL of cross-domain datatypes
* DAGR for cross-domain rules

## Aspect Design and Paritioning Scheme/Glue

* Do additional checks in constructor
  - For classes assigned to an enclave, we may need to check instances are created only in the assigned enclave

* Wrap constructor call into RPC
  - For constructor in cut, create aspect that will replace it with the constructor for a corresponding shadow object
  - This shadow's constructor must choose and ID and have the remote side cosntruct the object and associate with the ID
  - Shadow must also have a finalize method that will tell the remote side to release reference to the object
  - Shadow must "implement" (RPC-wrap) declared and inherited methods (including polymorphic forms) of class being shadowed if those
    methods can be called cross-domain
  - For now, if a constructor is in the cut, we may need to force all subclasses to be on the same side

```
// -- UNPARTITIONED
extra <- obj1 // Extra()
extra <- NULL
// obj1 can be garbage collected

// .........................
// -- PARTITIONED

extra <- obj1 // ExtraShadow()  Shadow<Extra>()
// ExtraShadow constructor has ID XX, calls RPC to have Extra() constructed on remote side and sends XX
// Remote constructs a new Extra() instance Y1, and maintains a hashmap XX <=> Y1
// any few future method requests can reference XX and can be dispatched to Y1
extra <- NULL
// ExtraShadow is garbage collected, and its finalize method will be called
// finalize method calls an RPC releasing XX 
// Remote removes XX <=> Y1 reference
// THis will cause Y1 to be garbage collected on remote side
```
* Wrap method call in RPC inside shadow's method
  - replace local method call with RPC 
  - marshall ID and input params into message and send
  - receive response and unmarshall back into local parameters and return value

```
 class Foo(){
  public int methodA(int k, ArrayList<String> j) {
    j.add(0, "Brilliant!");
    return 29;
  }
 }

 class FooShadow {
  // provide constructor which will do RPC
  // provide finalize which will do RPC
  // provide methodA
  public int methodA(int k, ArrayList<String> j) {
    // marshall/serialize ID, k, and j and send to other side
    // receive return value, and j from pther side
    // deserialize/unmarshall received values into k, j, and return
  }
  
  }
```

* Create remote main with handler threads
  - Maintain map of remote object ID to local memory reference
  - handle incoming messages and dispatch methods with (unmarshalled) parameters to relevant object
 
* Software content filtering (e.g., Signal app)
  - Based on CLE JSON do allow, block, redact processing 
  - Make it configurable on whether RPC call or RPC handler does the filtering (egress vs. ingress vs. neither)

* Handling generic containers (which may contain an object that will need to be remote) is TBD ...


## Alternative Scheme -- complicated, but may help with generics

* Create ExtraShadow to be identical to Extra, then wrap all methods and fields of Extra to call ExtraShadow
* Copy Extra's label to ExtraShadow and modify Extra's label to change it's level to caller side level
* Local side has modified Extra, remote side has ExtraShadow as well as Extra
* Now ArrayList<Extra> can be on both sides, on one side it will use the Aspect modified version of Extra and on
 the other side the original version 
* Need to be worked out in more detail 
 
## A better alternative to resolve generics and avoid changes to source code (only Aspects)

* There is no Shadow class as such.
* There will be a copy of the class on each side
* On the side to which the class is assigned, handlers will be generated
* On all other enclaves the class will be modified (into a shadow) using an aspect that wraps accesses to all fields and methods with around.
* The aspect will replace allowed accesses with RPCs and deny the rest with a violation Exception.


