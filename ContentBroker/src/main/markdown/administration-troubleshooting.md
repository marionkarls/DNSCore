As you have already read in the administration.md (if you don't have, do it first),
the WorkArea is the space where the ContentBroker manipulates the contents of objects. In a step by step
transition, it transforms contents of objects. Depending on the workflow, that means adding data
(by conversion or adding metadata), or removing of data (for DIPs or presentation). In any case the main unit
of operation is an unpacked object which comes either from the IngestArea or the LZAArea or both (in case of deltas).
To achieve the goal of transforming an object into a desired final state, the object has to go through one of
the possible workflows of the ContentBroker. Any workflow consists of small, well defined steps. Because
the actions in workflows can vary slightly over time, we will try to not mention the specifics in this document.
Instead, please consider the sources if you want to know more about the structure of the workflows:

https://github.com/da-nrw/DNSCore/blob/master/ContentBroker/src/main/resources/META-INF/beans-actions.xml (Ingest-Workflow)

### Ingest Workflow - Error handling

The reasons for an action to result in an error state could be various. Most of the times one encounters an action
in an error state xx1 or xx3 is when one of the services the ContentBroker depends on is not reachable or acts not as expected
(for example when a converter fails to convert a document). Normally it is a good advise for administrators new to the system
to see the logfile for the object before taking any further action, in order to understand the possible causes of errors better
and gain experience how to deal with certain error states. In order to find the logs for a given object, DAWeb shows the objects
technical id [oid] at the left hands side of a queue entries column. On your local file system you'll find the corresponding log at
[ContentBroker]/log/object-logs/[oid].log. 

#### xx0 
Any state ending with 0 means the object is in a well defined state, waiting for the ContentBroker to grab the object to take further
actions at any time the ContentBroker decides it has free capacities to to so.

#### xx1
Any state ending with 1 means there was an error, but the ContentBroker was able to execute the action specific rollback mechanism so
the unpacked object is an well defined state again. So, a job in state 120 and a job in state 121 correspond both to an object in the same
physical file system and database state.

#### xx2
This is the working state

#### xx4
These are states where an error occured due to imcomplete or inconsistent data caused by the user. The xx4 states always result 
of an action throwing a UserException. In case of such an exception the system autogenerates an error reports which gets instantly
delivered to the user via email. If DAWeb encounters an action in a xx4 state, it presents a "delete" button to the admins who are free
then to clean up the object from the queue. Depending if the object is a new one or a delta to an existing one either the newest package
or the whole objects gets deleted from the database if one clicks the delete button. Also the WorkArea gets clean up. In the case it was
a new object the orig name is reusable again. The urn which was given to the object is waste.

#### 123 - 353 (only xx3)


#### Deletion

