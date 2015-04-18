<?php

require_once "scripts/php/database_inc.php";
$trace = '';
$mysqli = null;


$jsoninobj = null;
$jsoninstr = null;
$jsoninstr = file_get_contents('php://input');  // incoming, raw payload, obviate $_POST[] vars. 
$jsoninobj = json_decode($jsoninstr);           // objectify incoming
//action = Subject-Verb-Object (use English-like Linguistics)
$action = $jsoninobj  -> action[0];
$subject = $action -> sbj; $verb = $action -> vrb; $object = $action -> obj;  //echo('s:'.$subject.',v:'.$verb.',o:'.$object);
$trace = "<TRACE>".$subject." - ".$verb." - ".$object."   ";


//TODO: add emailsubscription column to users table - active=mail; inactive=donotmail

// English linguistics rules: the $subject will $verb the $object
switch ($subject){
   case 'mysql':   // mysql is the subject and will do something
      switch($verb){
		// LOGIN =======================
         case 'login':
            $trace.=' case:login   ';
         	switch ($object){
               case 'user':
                  $trace.=' case:user   ';
                  echo mysql_login_user($jsoninobj->data[0]);
				  return;	
                  break; 
               default:
                  break; 
               }
               break;
		// REGISTRATION ===================
         case 'register':
             $trace.=' case:register   ';
        	 switch ($object){
                  case 'user':
               	     $trace.=' case:user   ';
               	     echo mysql_register_user($jsoninobj->data[0]); // -1=user email exists, 0=no user/some failure, >0=account created for user/n=new  userid
					 
                     break; 
                  default:
                     break; 
               }
         break;
         default:
            break;  
        }
	case 'ncbi': //ncbi serach - testbed for chron ops
		switch ($verb){
			case 'search':
			    $trace.=' case:search   ';
				echo ncbi_esearch();
				break;
			default:
				break;
			}
	  default:    	
         break;
     }

 return;
   
 $mysqli = null;
 
 function mysql_login_user($field_data){
 	global $trace; $trace.=' mysql_login_user()   uid='.$field_data->u.', pw='.$field_data->p;
 	global $mysqli;
 	OpenMySQLi();
 	$stmt = $mysqli->stmt_init();
	// user exist?
	$sql = "select idusers, email from dsmpubsearch.users where email = ? AND password = ?";
 	if(!$stmt->prepare($sql)){
 		$error = lastError();
 		echo('err:'.$error);
 		error_log("SQL error in mysql_login_user():$error");
 		die;
 	}
 	$stmt->bind_param('ss',	$field_data->u, $field_data->p);
 	$stmt->execute();
	$stmt->bind_result($idusers, $email);
	// if user exists return user data
	if (($row = $stmt->fetch()) === true){ 
		// the SQL UNION of all terms tables
		$sql = " Select 'bspecialty' as search, bs1.specialty from dsmpubsearch.basesearch bs1
				where bs1.uid = ".$idusers." AND bs1.active=1 UNION
				Select 'bterms' as search, bs2.terms from dsmpubsearch.basesearch bs2
				where bs2.uid = ".$idusers." AND bs2.active=1 UNION
				Select 'disease' as search, value from dsmpubsearch.advdisease dis  
				where dis.uid = ".$idusers." AND dis.active=1 UNION 
				Select 'drug', value from dsmpubsearch.advdrugs drg 
				where drg.uid = ".$idusers." AND drg.active=1 UNION 
				Select 'publication', value from dsmpubsearch.advpublications pub 
				where pub.uid = ".$idusers." AND pub.active=1 UNION 
				Select 'specialty', value from dsmpubsearch.advspecialty spc 
				where spc.uid = ".$idusers." AND spc.active=1 UNION 
				Select 'toxicology', value from dsmpubsearch.advtoxicology tox 
				where tox.uid = ".$idusers." AND tox.active=1";
 	if(!$stmt->prepare($sql)){
 		$error = lastError();
 		echo('err:'.$error);
 		error_log("SQL error in mysql_login_user():$error");
 		die;
 	}

	$stmt->execute();
	$stmt->bind_result($key, $val);			
	$json = '';	$k1 = ''; $key=''; $val='';
	
	//$json is an array of JSON objects; 
	// 1. each object is a search category {basic_specialty|basic_terms|adv_specialty|adv_disease|adv_drugs|adv_toxicology]
	// 2. each category object is an array of terms ( e.g. {"specialty":["pediatrics","anesthesiology","surgery"],"drugs":['drug1","drug2"],"etc":[...]}	
	
	// fetch 1st row, initialize a category $k1-$key; initialize JSON with 1st row values.
	if ($stmt->fetch() === true){ $k1=$key; $json.= "\"$key\":[".$val;}
	// get the rest of the rows if any...
	while ($stmt->fetch()) {
		// the sql union returns rows with 2 columns: col1=search_category, col2=search term
		// col1 never has quotes (we assigned this value in the sql; col2 may have quotes around compounded terms, preserve the quotes.
		// if we're in the same subject category{} add a new term to it's array[]
		if ($k1 == $key){
			$json.= ",".$val;
			$k1=$key;
			}
		// else start a new object with an array as it's value.
		else { 	
			$json.="],\"".$key."\":[".$val;
			$k1 = $key;
			}
	}
	$stmt->close();
	return "{\"user\":{\"uid\":".$idusers.", \"usr\":\"".$email."\"},\"searchterms\":{".$json."]}}";
		
		}
	$stmt->close();	
	//TODO: populate JSON error object with something more useful??
	$trace.=" mysql_login_user->fetch fails. uid=".$field_data->u." and pwd=".$field_data->p;
	return "{\"error\":\"".$trace."\"}";
 }

 function mysql_register_user($field_data){
  
	// setup new user record in Users table; populate search query tables with search terms; 
	// the nightly search-and-email engine uses the serachqueries table; the other tables are data-mined for advert targeting...
 
	// User accounts are identified by a unique email address.
	// (TODO:) clientside on lose focus email input check if email exists? yes:login; no:continue.
	// RETURN return -1 if email address already exists; return new userid (n>0) if new account created;
	// TODO - transaction rollback; return 0 on mysql failure.

	// populate base and advanced search tables; create full search query and populate searchqueries table.
 	global $trace; $trace.=' mysql_register_user()   ';
 	global $mysqli;
 	OpenMySQLi();
	$query = ''; //the full search query

	 //check for existing account by email, if email exists exit, return -1
	if($mysqli->query("SELECT email FROM dsmpubsearch.users where email like '".$field_data->e."';")->num_rows > 0 ){return -1;} 
	// in new account create new user, get user's userid to update search terms tables
	//TODO: make the users table insert and the terms tables inserts 1 transaction??
 	$stmt = $mysqli->stmt_init();
	$sql = "insert into dsmpubsearch.users (email, password, createdate) values (?,?,?)";
 	if(!$stmt->prepare($sql)){
 		$error = lastError();
 		echo('err:'.$error);
 		error_log("SQL error in mysql_register_user():$error");
 		die;
 	}
	$dt = date("Y-m-d H:i:s");
 	$stmt->bind_param('sss', $field_data->e, $field_data->p, $dt);
 	$stmt->execute();
 	$stmt->close();
	$uid = $mysqli->insert_id;

	// populate base search table
	$stmt = $mysqli->stmt_init();
	$sql = "insert into dsmpubsearch.basesearch (uid, specialty, terms, active, createdate) values (?,?,?,?,?)";
 	if(!$stmt->prepare($sql)){
 		$error = lastError();
 		echo('err:'.$error);
 		error_log("SQL error in mysql_register_user():$error");
 		die;
 	}

	$bsrch = 1;
	// add quotes to values - when we populate user data (eg at login for the user profile)
	// we populate a JSON object's search-term values with strings that are pre-quoted in the dBase.
	// this method is because advanced terms can be compounded within a set of quotes. user-added embedded quotes should pass thru untouched.
	$bspec = "\"".$field_data->searchterms->specialty."\"";
	$bterms = "\"".$field_data->searchterms->terms."\"";
 	$stmt->bind_param('issis', $uid, $bspec, $bterms, $bsrch, $dt );
 	$stmt->execute();
 	$stmt->close();
	$nrow = $mysqli->affected_rows;
	
	// 20140727 - because of latest lew change-order: Quickfix: only use the specialty and subspecialty to create saved query for now...
	//            moved this down to the subspecialty parse section
	$query .= $field_data->searchterms->specialty;
	
	// subspecialties - key 'subspeciality always exists, but can be an empty array
	// TODO: refactor to lowercase "Subspecialty"
	$sql = "";
	$ass =  $field_data->searchterms->Subspecialty;
	//return var_dump($ass);
	$vals = "";
	$lass = sizeof($ass);
	//echo('sizeof:'.$lass.'\n');
	$i = 0;
	//todo refactor multiinsert unparameterized using call_user_func_array() to bind multi inserts values...
	$subspec = "";
	if ($lass>0){
	// update the query string for the searchqueries table which is used for the chron search-and-email job
		$query .= ' AND ('.join(" OR ", $ass). ')';
		
		for( ; $i<$lass; $i++){
			str_replace($ass[$i], "'","''");
			$vals.= '('.$uid.', \'"'.$ass[$i].'"\',\''.$dt.'\'),';  // all search terms indbase are quoted with double-quotes as part of the string for JAVA chron job
			}
		$vals = substr($vals,0,-1); // strip last comma	
		$sql = "insert into dsmpubsearch.subspecialty (uid, subspecialty, createdate) values ".$vals;
		if ($mysqli->query($sql) === TRUE){$nrow += $mysqli->affected_rows; };
	}
	
	// populate advanced search terms tables if user submitted advanced search terms
	$at = null;
	if(array_key_exists("advterms",$field_data)){
		$at = $field_data->advterms;
		// advterms is a collection of topic headings [Specialty|disease|drug|tox|pub]
		// each topic-heading is an array of terms under the headings categgory {specialty:[pediatrics|cardiology|optometry]}
		$nrow=0; 
		foreach($at as $table => $terms ){
			// $k is a subject category; $v is an array of values for that subject
			// mysql tables named for category, prepend with 'adv' for advanced searches.
			// $tmp.= $k.' = '.count($v).';  ';
			// TODO: batch parameterize?
			$query .= ($table == 'publications') ? ' AND (' : ' OR ('; 
			$c = count($terms); $i=0; $vals = '';
			for( ; $i < $c; $i++){
				$vals.= '('.$uid.', \''.$terms[$i].'\', 1, \''.$dt.'\'),';
				$query .= trim($terms[$i]).' OR ';
				}
			$query =substr($query, 0, -3);  // strip last OR+space
			$query .= ($table == 'publications') ? '[journal])' : ')';	
			$sql = 'insert into dsmpubsearch.adv'.$table.' (uid, value, active, createdate) values '.substr($vals, 0, -1).';'; //strip last comma from $vals
			if ($mysqli->query($sql) === TRUE) {$nrow += $mysqli->affected_rows; };
			}
		}
	
	// a complex or compounded query - a macro of the micros, the nightly search-and-email engine derives it's search terms from dsmpubsearch.searchqueries table.
	$query = ' insert into dsmpubsearch.searchqueries (uid, query, createdate, active) values ('.$uid.', \''.$query.'\', \''.$dt.'\', 1);';
	if ($mysqli->query($query) === TRUE){$nrow += $mysqli->affected_rows; };

	return $uid;
 }	
	
	
	
?>


