<?php
   
/*
// STAGING:  Stage, read and write user, dev db...
if( strpos($_SERVER['SERVER_NAME'], 'localhost') === false){
	$ST_MYSQL_HOST   = '';
	$ST_MYSQL_USER   = '';
	$ST_MYSQL_PASS   = '';
	$ST_MYSQL_DB_NAME= '';
} else {
*/
// there are 2 users 'dsmpubs', same password:
// 1. is dsmpubs@localhost
// 2. is dsmpubs@% for development from remote machines using production mysql
// TODO: remove user 'dsmpubs@%' from production mysql when deploying to production

	$SB_MYSQL_HOST   = 'localhost';
	$SB_MYSQL_USER   = 'dsmpubs';
	$SB_MYSQL_PASS   = 'sbu9m5d';
	$SB_MYSQL_DB_NAME= 'dsmpubsearch';


function OpenMySQL(){
    global $mLink;
    global $SB_MYSQL_HOST,$SB_MYSQL_USER, $SB_MYSQL_PASS,$SB_MYSQL_DB_NAME;
    $mLink = mysql_connect($SB_MYSQL_HOST,$SB_MYSQL_USER, $SB_MYSQL_PASS);
    if(mysql_errno($mLink))
        die(mysql_error($mLink));
    mysql_select_db($SB_MYSQL_DB_NAME, $mLink);
    if(mysql_errno($mLink))
        die(mysql_error($mLink));
}
function OpenMySQLi(){
    global $mysqli;
    global $SB_MYSQL_HOST,$SB_MYSQL_USER, $SB_MYSQL_PASS,$SB_MYSQL_DB_NAME;
//    echo "opening...";
    $mysqli = new mysqli($SB_MYSQL_HOST, $SB_MYSQL_USER, $SB_MYSQL_PASS, $SB_MYSQL_DB_NAME);
        if (mysqli_connect_errno()) die("OpenMySQLi() failed: <br />".mysqli_connect_error());
}

function DoMySQLQuery($sql, $result_type = MYSQL_BOTH) {
    // $this->print_r_XML_comment($sql);
    global $mLink;
    $result = mysql_query($sql, $mLink);
    if(mysql_errno($mLink))
    	die(mysql_error($mLink));
    $phpResult = array();
    while ($row = mysql_fetch_array($result, $result_type)) {
	    // $this->print_r_XML_comment($row);
    	$phpResult[] = $row;
    }
    mysql_free_result($result);
    if(mysql_errno($mLink))
	die(mysql_error($mLink));
    return $phpResult;
}

function DoMySQLiQuery($sql, $result_type = MYSQL_BOTH) {
    // $this->print_r_XML_comment($sql);
    global $mysqli;
    $result = mysqli_query($sql, $mysqli);
    if(mysqli_errno($this->mLink))
    	die(mysqli_error($mysqli));
    $phpResult = array();
    while ($row = mysqli_fetch_array($result, $result_type)) {
	    // $this->print_r_XML_comment($row);
    	$phpResult[] = $row;
    }
    mysqli_free_result($result);
    if(mysqli_errno($mysqli))
	    die(mysqli_error($mysqli));
    return $phpResult;
}

function print_r_XML_comment($expression, $return=false) {
    if ($return) {
	    return "<!--\n".print_r($expression,true)."\n-->\n";
    } else {
	    echo "<!--\n";
    	print_r($expression,false);
	    echo "\n-->\n";
    }
}

function is_date( $str ) {
  $stamp = strtotime( $str );
  if (!is_numeric($stamp))  {
     return FALSE;
  }
  $month = date( 'm', $stamp );
  $day   = date( 'd', $stamp );
  $year  = date( 'Y', $stamp );
  if (checkdate($month, $day, $year))   {
     return TRUE;
  }
  return FALSE;
}




?>
