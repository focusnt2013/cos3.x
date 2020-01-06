/**
 * This version of Software is free for using in non-commercial applications. 
 * For commercial use please contact icewee@tom.com to obtain license
 * author: IceWee
 * blog: http://icewee.cnblogs.com
 * Date: 2011-04-22 17:40:25 (Fri, 22 Apri 2011)
 */ 
function googleSuggest(data) {
	this.$settings = {
		input: null,
		url: null,
		paramName: null,
		dataArray: new Array(),
		maxShowRows: 30,
		showText: '',
		showTextColor: '#999999',
		showTextFontStyle: 'italic',
		showTextOffsetLeft: 0,
		focusItemBgColor: '#D5E2FF',
		blurItemBgColor: '#FFFFFF',
		suggestBorderColorLT: '#A2BFF0',
		suggestBorderColorRB: '#558BE3',
		isDynamicData: false,
		itemFontSize: '12pt',
		isSortData: true,
		isObjectArray: false,
		callback: null,
		isCaseSensitive: false
	}
	this.$isIE = true;
	this._copySettings(data);
	this.$suggest = null;	// suggest div
	this.$focusItemIndex = -1;
	this.$DOWN = '40';	// keyboard keyCode down
	this.$UP = '38';	// keyboard keyCode up
	this.$ENTER = '13';	// keyboard keyCode Enter
	this.$xmlHttp = null;	// ajax object
	this.$HTTP_STATE_UNINITIALIZED = 0;	// xmlHttpRequest object is not be initilaized
	this.$HTTP_STATE_LOADING = 1;
	this.$HTTP_STATE_LOADED = 2;
	this.$HTTP_STATE_INTERACTIVING = 3;
	this.$HTTP_STATE_COMPLETED = 4;
	this.$HTTP_STATUS_OK = 200;
	return this;
}

/**
 * @desc: working now!
 */
googleSuggest.prototype.go = function() {
	if (this._isCanGo()) {
		this._initSuggest();
		this._initInput();
	} else {
		alert('initialize failed!');
	}
};

/**
 * @desc: copy params that user specified
 * 
 * @param {Object} data
 */
googleSuggest.prototype._copySettings = function(data) {
	if (typeof(data.input) == 'string')
		this.$settings.input = document.getElementById(data.input);
	else
		this.$settings.input = data.input;
	if (data.isSortData && data.isSortData == true) {
		this.$settings.isSortData = true;
	} else {
		this.$settings.isSortData = false;
	}
	if (data.isObjectArray && data.isObjectArray == true) {
		this.$settings.isObjectArray = true;
	} else {
		this.$settings.isObjectArray = false;
	}
	if (data.isCaseSensitive && data.isCaseSensitive == true) {
		this.$settings.isCaseSensitive = true;
	} else {
		this.$settings.isCaseSensitive = false;
	}
	if (data.isDynamicData && data.isDynamicData == true) {	// dynamic data
		this.$settings.isDynamicData = true;
		if (this._isNotBlank(data.url))
			this.$settings.url = data.url;
		if (this._isNotBlank(data.paramName))
			this.$settings.paramName = data.paramName;
	} else {
		this.$settings.isDynamicData = false;
		if (data.dataArray && data.dataArray.constructor == Array && data.dataArray.length > 0) {
			this.$settings.dataArray = data.dataArray;
			this._sortArray();	// sort the array or not
		}
	}
	if (this._isNotBlank(data.focusItemBgColor))
		this.$settings.focusItemBgColor = data.focusItemBgColor;
	if (this._isNotBlank(data.blurItemBgColor))
		this.$settings.blurItemBgColor = data.blurItemBgColor;
	if (this._isNotBlank(data.showText))
		this.$settings.showText = this._trim(data.showText);
	if (this._isNotBlank(data.showTextColor))
		this.$settings.showTextColor = data.showTextColor;
	if (this._isNotBlank(data.showTextFontStyle))
		this.$settings.showTextFontStyle = data.showTextFontStyle;
	if (this._isValidNum(data.maxShowRows))
		this.$settings.maxShowRows = data.maxShowRows;
	if (this._isValidNum(data.showTextOffsetLeft))
		this.$settings.showTextOffsetLeft = data.showTextOffsetLeft;
	if (this._isNotBlank(data.suggestBorderColorLT))
		this.$settings.suggestBorderColorLT = data.suggestBorderColorLT;
	if (this._isNotBlank(data.suggestBorderColorRB))
		this.$settings.suggestBorderColorRB = data.suggestBorderColorRB;
	if (this.$settings.input) {
		if (this._isNotBlank(data.itemFontSize))
			this.$settings.itemFontSize = data.itemFontSize;
		else if (this._isNotBlank(this._getInput().style.fontSize))
			this.$settings.itemFontSize = this._getInput().style.fontSize;
	}
	if (typeof(data.callback) == 'function'){
		this.$settings.callback = data.callback;;
	}else{
		if (this._isNotBlank(data.callback))
			this.$settings.callback = eval(data.callback);
	}
	this.$isIE = ((document.all) ? true : false) || navigator.userAgent.indexOf('MSIE') > 0;
};

/**
 * @desc: validate important params, whether can run script
 * 
 * @return true or false
 */
googleSuggest.prototype._isCanGo = function() {
	if (!this._getInput())	// invalid input
		return false;
	if (this.$settings.isDynamicData) {	// dynamic data
		if (this._isBlank(this.$settings.url) || this._isBlank(this.$settings.paramName))
			return false;
	} else {	// static data
		if (this.$settings.dataArray.length == 0) {
			return false;
		} else {
			// object array, formater eg:[{id: '1', name: 'java'}], only accept/process id and name property
			if (this.$settings.isObjectArray) {
				// here only validate the first element of array, so... I do not say whatever you know
				if (!this.$settings.dataArray[0].id || !this.$settings.dataArray[0].name)
					return false;
			}
		}
	}
	return true;
};

/**
 * @dese: sort the data array
 */
googleSuggest.prototype._sortArray = function() {
	if (this.$settings.isSortData) {
		if (!this.$settings.isObjectArray) {	// string array
			this.$settings.dataArray.sort(function(a, b) {	// sort the array
				if (a.length > b.length)
					return 1;
				else if (a.length == b.length)
					return a.localeCompare(b);
				else
					return -1;
			});
		} else {	// object array
			if (this.$settings.dataArray[0].id && this.$settings.dataArray[0].name) {
				this.$settings.dataArray.sort(function(a, b) {	// sort the array
					if (a.name.length > b.name.length)
						return 1;
					else if (a.name.length == b.name.length)
						return a.name.localeCompare(b.name);
					else
						return -1;
				});
			}
		}
	}
};

/**
 * @desc: get current input
 * 
 * @return current input object
 */
googleSuggest.prototype._getInput = function() {
	return this.$settings.input;
};

/**
 * @desc: force blur the input
 */
googleSuggest.prototype._forceBlurInput = function() {
	this._getInput().blur();
};

/**
 * @desc: initinalize the suggest
 */
googleSuggest.prototype._initSuggest = function() {
	var input = this._getInput();
	var left = this._getAbsolutePosition(input)[0];
	var top = this._getAbsolutePosition(input)[1];
	var width = this._getWidthAndHeight(input)[0];
	var height = this._getWidthAndHeight(input)[1];
	this.$suggest = document.createElement('div');
	document.body.appendChild(this.$suggest);
	this.$suggest.style.position = 'absolute';
	this.$suggest.style.borderWidth = '1px';
	this.$suggest.style.borderStyle = 'solid';
	this.$suggest.style.borderLeftColor = this.$settings.suggestBorderColorLT;
	this.$suggest.style.borderTopColor = this.$settings.suggestBorderColorLT;
	this.$suggest.style.borderRightColor = this.$settings.suggestBorderColorRB;
	this.$suggest.style.borderBottomColor = this.$settings.suggestBorderColorRB;
	if (this.$isIE)
		this.$suggest.style.width = width + 'px';
	else
		this.$suggest.style.width = width - 2 + 'px';
	this.$suggest.style.left = left + 'px';
	this.$suggest.style.top = top + height - 1 + 'px';
	this.$suggest.style.display = 'none';
};

/**
 * @desc: init input and bind mouse and keyboard event
 */
googleSuggest.prototype._initInput = function() {
	var input = this._getInput();
	input.suggest = this;
	input.value = this.$settings.showText;
	input.style.color = this.$settings.showTextColor;
	input.style.fontStyle = this.$settings.showTextFontStyle;
	input.onfocus = this._displaySuggest;
	input.onblur = this._inputBlur;
	input.onkeyup = this._catchKeyCode;
};

/**
 * @desc: current object may be input or suggest
 */
googleSuggest.prototype._displaySuggest = function() {
	var o;
	if (this.$suggest) {	// suggest
		o = this;
	} else {
		o = this.suggest;	// input
	}
	if (!o.$settings.isDynamicData)	// static data
		o._staticSuggest();
	else
		o._dynamicSuggest();
};

/**
 * @desc: create suggest with the static data array
 */
googleSuggest.prototype._staticSuggest = function() {
	this.$focusItemIndex = -1;
	var inputVal = this._getInput().value;
	inputVal = this._trim(inputVal);
	if (this._isNotBlank(inputVal)) {
		if (inputVal != this.$settings.showText) {
			this._getInput().style.color = '#000000';
			this._getInput().style.fontStyle = 'normal';
			this.$suggest.innerHTML = '';
			this._createSuggestItems(inputVal);
			if (this.$suggest.children.length > 0)
				this._showSuggest();
			else
				this._hideSuggest();
		} else {
			this._getInput().style.color = this.$settings.showTextColor;
			this._getInput().style.fontStyle = this.$settings.showTextFontStyle;
			this._getInput().value = '';
			this._hideSuggest();
		}
	} else {
		this._hideSuggest();
	}
}

/**
 * @desc: create suggest, with the url to get dynamic data array
 */
googleSuggest.prototype._dynamicSuggest = function() {
	this.$focusItemIndex = -1;
	var inputVal = this._getInput().value;
	inputVal = this._trim(inputVal);
	if (this._isNotBlank(inputVal) ) {
		if (inputVal != this.$settings.showText) {
			this._getInput().style.color = '#000000';
			this._getInput().style.fontStyle = 'normal';
			this.$suggest.innerHTML = '';
			var url = this._constructUrl() + inputVal;
			var xmlHttp = this._getAjax();
			xmlHttp.suggest = this;
			xmlHttp.onreadystatechange = function() {
				try{ 
					if (xmlHttp.readyState == this.suggest.$HTTP_STATE_COMPLETED) {
						if (xmlHttp.status == this.suggest.$HTTP_STATUS_OK) {
							var array = xmlHttp.responseText;
							if (this.suggest._isNotBlank(array)) {
								this.suggest.$settings.dataArray = eval(array);
								this.suggest._sortArray();	// sort or not
								var size = 0;
								if (this.suggest.$settings.dataArray > this.suggest.$settings.maxShowRows + 1)
									size = this.suggest.$settings.maxShowRows + 1;
								else
									size = this.suggest.$settings.dataArray.length;
								var o, item;
								if (this.suggest.$settings.isObjectArray) {
									for (var i = 0; i < size; i++) {
										o = this.suggest.$settings.dataArray[i];
										if (typeof(o) == 'object') {
											if (!o.id && !o.name) {
												alert('Invalid format! The object must has id and name property, eg: [{id: 1, name: \'peter\'}, ...]');
												this.suggest._forceBlurInput();
												break;
											} else {
												item = this.suggest._createSuggestItem(o.name, inputVal);
												this.suggest.$suggest.appendChild(item);
											}
										} else {
											alert('Invalid format! Correct format is like this [{id: 1, name: \'peter\'}, {id: \'admin\', name: \'administrator\'}]');
											this.suggest._forceBlurInput();
											break;
										}
									}
								} else {	// string array
									for (var i = 0; i < size; i++) {
										o = this.suggest.$settings.dataArray[i];
										if (typeof(o) == 'string') {
											item = this.suggest._createSuggestItem(o, inputVal);
											this.suggest.$suggest.appendChild(item);
										} else {
											alert('Invalid format! Correct format is like this [\'java\', \'javascript\', \'javaeye\']');
											this.suggest._forceBlurInput();
											break;
										}
									}
								}
								if (this.suggest.$suggest.children.length > 0)
									this.suggest._showSuggest();
								else
									this.suggest._hideSuggest();
							} else {
								this.suggest._hideSuggest();
							}
						}
					}
				} catch (e) {alert(e);}
			}
			xmlHttp.open('GET', url, true);
		    xmlHttp.send(null);
		} else {
			this._getInput().style.color = this.$settings.showTextColor;
			this._getInput().style.fontStyle = this.$settings.showTextFontStyle;
			this._getInput().value = '';
			this._hideSuggest();
		}
	} else {
		this._hideSuggest();
	}
}

/**
 * @desc: create the items show in the suggest, this method is used to static data, js fiter
 * 
 * @param {Object} inputVal, input keywords
 */
googleSuggest.prototype._createSuggestItems = function(inputVal) {
	var size = 0;
	if (this.$settings.dataArray > this.$settings.maxShowRows + 1)
		size = this.$settings.maxShowRows + 1;
	else
		size = this.$settings.dataArray.length;
	var reg = this._getRegExps(inputVal);
	var o, item;
	if (this.$settings.isObjectArray) {
		for (var i = 0; i < size; i++) {
			o = this.$settings.dataArray[i];
			if (typeof(o) == 'object') {
				if (!o.id && !o.name) {
					alert('Invalid format! The object must has id and name property, eg: [{id: 1, name: \'peter\'}, ...]');
					this._forceBlurInput();
					break;
				} else {
					item = this._createSuggestItem(o.name, inputVal);
					this.$suggest.appendChild(item);
				}
			} else {
				alert('Invalid format! Correct format is like this [{id: 1, name: \'peter\'}, {id: \'admin\', name: \'administrator\'}]');
				this._forceBlurInput();
				break;
			}
		}
	} else {	// string array
		for (var i = 0; i < size; i++) {
			o = this.$settings.dataArray[i];
			if (typeof(o) == 'string') {
				if (o.match(reg)) {
					item = this._createSuggestItem(o, inputVal);
					this.$suggest.appendChild(item);
				}
			} else {
				alert('Invalid format! Correct format is like this [\'java\', \'javascript\', \'javaeye\']');
				this._forceBlurInput();
				break;
			}
		}
	}
};

/**
 * @desc: create a item that show in suggest
 * 
 * @param {Object} value, one value of the data array
 * @param {Object} inputVal, input keywords
 * @return item
 */
googleSuggest.prototype._createSuggestItem = function(value, inputVal) {
	var v = value.replace(inputVal, '<span style="font-weight: normal;">' + inputVal + '</span>');
	var item = document.createElement('div');
	item.innerHTML = v;
	item.style.color = '#000000';
	item.style.backgroundColor = this.$settings.blurItemBgColor;
	item.style.fontWeight = 'bold';
	item.style.fontSize = this.$settings.itemFontSize;
	item.style.paddingLeft = this.$settings.showTextOffsetLeft + 'px';
	item.onmouseover = this._focusItem;
	item.onmouseout = this._blurItem;
	item.onmousedown = this._returnSelectedItem;
	item.suggest = this;
	return item;
};

/**
 * @desc: current object is suggest item(eg:<div>java</div>)
 */
googleSuggest.prototype._focusItem = function() {
	this.style.backgroundColor = this.suggest.$settings.focusItemBgColor;
};

/**
 * @desc: return a Regular Expressions used to match keywords
 * 
 * @param {Object} value, string
 * @return regexps
 */
googleSuggest.prototype._getRegExps = function(value) {
	if (this.$settings.isCaseSensitive)
		return new RegExp(value, 'i');
	else
		return new RegExp(value);
};

/**
 * @desc: current object is suggest item(eg:<div>xxx</div>)
 */
googleSuggest.prototype._blurItem = function() {
	this.style.backgroundColor = this.suggest.$settings.blurItemBgColor;
};

/**
 * @desc: current object is suggest item(eg:<div>javascript</div>)
 */
googleSuggest.prototype._returnSelectedItem = function() {
	var name = this.innerHTML;
	var breg = new RegExp('<span style="font-weight: normal;">', 'i');	// <META content="IE=9" http-equiv="X-UA-Compatible">
	var mreg = new RegExp('<span style="font-weight: normal">', 'i');	// common, must remove ';'
	var ereg = new RegExp('</span>', 'i');
	name = name.replace(breg, '');
	name = name.replace(mreg, '');
	name = name.replace(ereg, '');
	this.suggest._getInput().value = name;
	var id = this.suggest._getIdbyName(name);
	if (this.suggest.$settings.callback)	// callback function
		this.suggest.$settings.callback(id, name);
	this.suggest._forceBlurInput();
	this.suggest._hideSuggest();
};

/**
 * @desc: whether the suggest is hide
 * 
 * @return true of false
 */
googleSuggest.prototype._isSuggestHide = function() {
	return this.$suggest.style.display == 'none';
};

/**
 * @desc: current object may be input or suggest, show the suggest
 */
googleSuggest.prototype._showSuggest = function() {
	if (this.$suggest)	// suggest
		this.$suggest.style.display = 'block';
	else	// input
		this.suggest.$suggest.style.display = 'block';
};

/**
 * @desc: current object may be input or suggest, hide the suggest
 */
googleSuggest.prototype._hideSuggest = function() {
	var o;
	if (this.$suggest) {	// suggest
		o = this;
	} else {	// input
		o = this.suggest;
	}
	o.$suggest.style.display = 'none';
};

/**
 * @desc: current object is input, when the input is blur that should be do?
 */
googleSuggest.prototype._inputBlur = function() {
	this.suggest._hideSuggest();
	var inputVal = this.value;
	if (this.suggest._isBlank(inputVal)) {
		this.value = this.suggest.$settings.showText;
		this.style.color = this.suggest.$settings.showTextColor;
		this.style.fontStyle = this.suggest.$settings.showTextFontStyle;
	}
};

/**
 * @desc: current object is input, catch the keyboard event, judge to show or hide suggest
 */
googleSuggest.prototype._catchKeyCode = function() {
	var keyCode = 0;
	if (this.suggest.$isIE) {
		var keyCode = event.keyCode;
		if (keyCode == this.suggest.$DOWN || keyCode == this.suggest.$UP) {	// down or up
			var isUp = true;
			if (keyCode == this.suggest.$DOWN)	// down
				isUp = false;
			this.suggest._changeFocusItem(isUp);
		} else if (keyCode == this.suggest.$ENTER) {
			this.suggest._catchEnter();
		} else {
			this.suggest._displaySuggest();
		}
	} else {
		this.suggest._displaySuggest();
	}
};

/**
 * @desc: used to catch enter event
 */
googleSuggest.prototype._catchEnter = function() {
	var name = this._getInput().value;
	var id = this._getIdbyName(name);
	if (this.$settings.callback)	// callback function
		this.$settings.callback(id, name);
	this._forceBlurInput();
	this._hideSuggest();
}

/**
 * @desc: change the focus item and change the input text's style that show in the suggest
 * 
 * @param {Object} isUp, keyboard event, up or down
 */
googleSuggest.prototype._changeFocusItem = function(isUp) {
	if (this._isSuggestHide()) {
		this._showSuggest();
	} else {
		if (isUp)
			this.$focusItemIndex--;
		else
			this.$focusItemIndex++;
	}
	var maxIndex = this.$suggest.children.length - 1;
	if (this.$focusItemIndex < 0) {
		this.$focusItemIndex = maxIndex;
	}
	if (this.$focusItemIndex > maxIndex) {
		this.$focusItemIndex = 0;
	}
	var breg = new RegExp('<span style="font-weight: normal;">', 'i');	// <META content="IE=9" http-equiv="X-UA-Compatible">
	var mreg = new RegExp('<span style="font-weight: normal">', 'i');	// common, must remove ';'
	var ereg = new RegExp('</span>', 'i');
	var text;
	for (var i = 0; i <= maxIndex; i++) {
		if (i == this.$focusItemIndex) {
			text = this.$suggest.children[i].innerHTML;
			text = text.replace(breg, '');
			text = text.replace(mreg, '');
			text = text.replace(ereg, '');
			this._getInput().value = text;
			this.$suggest.children[i].style.backgroundColor = this.$settings.focusItemBgColor;
		} else {
			this.$suggest.children[i].style.backgroundColor = this.$settings.blurItemBgColor;
		}
	}
};

/**
 * @desc: get a html element's absolute position
 * 
 * @param {Object} o, html element
 * @return array, value 1 is x coordinate and value 2 is y coordinate
 */
googleSuggest.prototype._getAbsolutePosition = function(o) {
	var array = new Array();
	var top = 0, left = 0;
	do {
		top += o.offsetTop;
		left += o.offsetLeft;
	} while (o = o.offsetParent);
	array[0] = left;
	array[1] = top;
	return array;
};

/**
 * @desc: get a html element's width and height
 * 
 * @param {Object} o, html element
 * @return array that first value is width and second value is height
 */
googleSuggest.prototype._getWidthAndHeight = function(o) {
	var array = new Array();
	array[0] = o.offsetWidth;
	array[1] = o.offsetHeight;
	return array;
};

/**
 * @desc: trim a string
 * 
 * @param {Object} string
 * @return string that remove the left space and right space, space(space and tab)
 */
googleSuggest.prototype._trim = function(string) {
	return string.replace(/(^\s*)|(\s*$)/g, '');
};

/**
 * @desc: validate a string is null or ''
 * 
 * @param {Object} string
 * @return true if is not '' or undefined, else false 
 */
googleSuggest.prototype._isValidString = function(string) {
	if (string && this._trim(string))
		return true;
	return false;
}

/**
 * @desc: validate number
 * 
 * @param {Object} num
 * @return true if is valid number, else false
 */
googleSuggest.prototype._isValidNum = function(num) {
	if (!isNaN(parseInt(num)))
		return true;
	return false;
}

/**
 * @desc: get an ajax object
 * 
 * @return ajax object, this.$xmlHttp or new XMLHttpRequest
 */
googleSuggest.prototype._getAjax = function() {
	if (!this.$xmlHttp) {
		this.$xmlHttp = this._getXMLHttpRequest();
	}
	if (this.$xmlHttp.readyState != this.$HTTP_STATE_COMPLETED) {
		return this._getXMLHttpRequest();
	}
	return this.$xmlHttp;
};

/**
 * @desc: create a XMLHttpRequest object
 * 
 * @return a new XMLHttpRequest object
 */
googleSuggest.prototype._getXMLHttpRequest = function() {
	var xmlHttp;
	if (window.XMLHttpRequest) {
    	xmlHttp = new XMLHttpRequest();
	} else if (window.ActiveXobject) {
    	try {
			xmlHttp = new ActiveXobject('MSXML2XMLHTTP');
   		} catch (e){
			try {
				xmlHttp = new ActiveXobject('Microsoft.XMLHttp');
			} catch (e){}
		}
   	}
	return xmlHttp;
}

/**
 * @desc: construct the full url with the user specified url and param name
 * 
 * @return full url
 */
googleSuggest.prototype._constructUrl = function() {
	var url = this.$settings.url;
	var paramName = this.$settings.paramName;
	if (url.indexOf('?') == -1)
		url += '?' + paramName + '=';
	else
		url += '&' + paramName + '=';
	return url;
};

/**
 * @desc: so... name must be unique, else will be wrong, see below you will be understand
 * 
 * @param {Object} name, object array name
 * @return object array id value
 */
googleSuggest.prototype._getIdbyName = function(name) {
	var id = null;
	var array = this.$settings.dataArray;
	for (var i = 0; i < array.length; ++i) {
		if (array[i].name == name)
			return array[i].id;
	}
	return id;
}

/**
 * blank means null/undefined/''/tab
 * 
 * @param {Object} string
 * @return true or false
 */
googleSuggest.prototype._isBlank = function(string) {
	if (string == undefined)	// null or un init
		return true;
	if (this._trim(string) == '')
		return true;
	return false;
}

/**
 * not blank
 * 
 * @param {Object} string
 * @return true or false
 */
googleSuggest.prototype._isNotBlank = function(string) {
	return !this._isBlank(string);
}
