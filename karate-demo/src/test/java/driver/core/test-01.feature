Feature: ui automation core capabilities

Background:
  * def webUrlBase = karate.properties['web.url.base']

Scenario Outline: using <config>
  * def config = <config>
  * set config.showDriverLog = true
  * configure driver = config

  Given driver webUrlBase + '/page-01'
  
  # wait for very slow loading element
  And assert wait('#eg01WaitId')

  # a powerful variants of the above, call any js on the element
  And assert wait('#eg01WaitId', "function(e){ return e.innerHTML == 'APPEARED!' }")
  And assert wait('#eg01WaitId', "_.innerHTML == 'APPEARED!'")
  And assert wait('#eg01WaitId', '!_.disabled')
  And match evaluate('#eg01WaitId', "function(e){ return e.innerHTML }") == 'APPEARED!'
  And match evaluate('#eg01WaitId', '_.innerHTML') == 'APPEARED!'
  And match evaluate('#eg01WaitId', '!_.disabled') == true

  # key events
  And input('#eg02InputId', Key.SHIFT)
  Then match text('#eg02DivId') == '16'
    
  # cookies
  * def cookie1 = { name: 'foo', value: 'bar' }
  And match driver.cookies contains '#(^cookie1)'
  And match cookie('foo') contains cookie1

  # set window size
  And driver.dimensions = <dimensions>

  # navigation and dom checks
  And input('#eg01InputId', 'hello world')
  When click('input[name=eg01SubmitName]')
  Then match text('#eg01DivId') == 'hello world'
  And match value('#eg01InputId') == 'hello world'  
  And match attribute('#eg01SubmitId', 'type') == 'submit'
  And match name('#eg01SubmitId') == 'INPUT'
  And match enabled('#eg01InputId') == true
  And match enabled('#eg01DisabledId') == false

  # clear before input
  When clear('#eg01InputId')
  And input('#eg01InputId', 'something else')
  And match value('#eg01InputId') == 'something else'  
  When value('#eg01InputId', 'something more')
  And match value('#eg01InputId') == 'something more'
  
  # refresh
  When refresh()
  Then match driver.location == webUrlBase + '/page-01'
  And match text('#eg01DivId') == ''
  And match value('#eg01InputId') == ''
  And match driver.title == 'Page One'

  # navigate to page and css checks
  When driver webUrlBase + '/page-02'
  Then match text('.eg01Cls') == 'Class Locator Test'
  And match html('.eg01Cls') == '<div class="eg01Cls" style="background-color: yellow"><span>Class Locator Test</span></div>'
  And match driver.title == 'Page Two'
  And match driver.location == webUrlBase + '/page-02'
  And match css('.eg01Cls', 'background-color') contains '(255, 255, 0'

  # set cookie
  Given def cookie2 = { name: 'hello', value: 'world' }
  When driver.cookie = cookie2
  Then match driver.cookies contains '#(^cookie2)'

  # delete cookie
  When deleteCookie('foo')
  Then match driver.cookies !contains '#(^cookie1)'

  # clear cookies
  When clearCookies()
  Then match driver.cookies == '#[0]'

  # back and forward
  When back()
  Then match driver.location == webUrlBase + '/page-01'
  And match driver.title == 'Page One'
  When forward()
  Then match driver.location == webUrlBase + '/page-02'
  And match driver.title == 'Page Two'

  # dialog - alert
  When click('^Show Alert', true)
  Then match driver.dialog == 'this is an alert'
  And dialog(true)

  # dialog - confirm true
  When click('^Show Confirm', true)
  Then match driver.dialog == 'this is a confirm'
  And dialog(false)
  And match text('#eg02DivId') == 'Cancel'

  # dialog - confirm false
  When click('^Show Confirm', true)
  And dialog(true)
  And match text('#eg02DivId') == 'OK'

  # dialog - prompt
  When click('^Show Prompt', true)
  Then match driver.dialog == 'this is a prompt'
  And dialog(true, 'hello world')
  And match text('#eg02DivId') == 'hello world'

  # screenshot
  * screenshot('#eg02DivId')

  # get element dimensions
  * match rect('#eg02DivId') == { x: '#number', y: '#number', height: '#number', width: '#number' }

  # new tab opens, wait for page
  When click('^New Tab')
  And waitForPage()

  # switch back to first tab
  When switchPage('Page Two')
  Then match driver.title == 'Page Two'
  And match driver.location contains webUrlBase + '/page-02'

  # submit - action that waits for page navigation
  When submit('*Page Three')
  And match driver.title == 'Page Three'
  And match driver.location == webUrlBase + '/page-03'

  # get html for all elements that match
  When def list = htmls('div div')
  Then match list == '#[3]'
  And match each list contains '@@data'

  # get text for all elements that match
  When def list = texts('div div')
  Then match list == '#[3]'
  And match each list contains '@@data'

  # get value for all elements that match
  When def list = values("input[name='data2']")
  Then match list == '#[3]'
  And match each list contains 'check'

  # select option with text
  Given select('select[name=data1]', '^Option Two')
  And click('input[value=check2]')
  When submit('#eg02SubmitId')
  And match text('#eg01Data1') == 'option2'
  And match text('#eg01Data2') == 'check2'

  # select option containing text
  Given select('select[name=data1]', '*Two')
  And click('[value=check2]')
  And click('[value=check1]')
  When submit('#eg02SubmitId')
  And match text('#eg01Data1') == 'option2'
  And match text('#eg01Data2') == '["check1","check2"]'

  # select option by value
  Given select('select[name=data1]', 'option2')
  When submit('#eg02SubmitId')
  And match text('#eg01Data1') == 'option2'

  # switch to iframe by index
  Given driver webUrlBase + '/page-04'
  And match driver.location == webUrlBase + '/page-04'
  And switchFrame(0)
  When input('#eg01InputId', 'hello world')
  And click('#eg01SubmitId')
  Then match text('#eg01DivId') == 'hello world'

  # switch back to parent frame
  When switchFrame(null)
  Then match text('#eg01DivId') == 'this div is outside the iframe'

  # switch to iframe by locator
  Given driver webUrlBase + '/page-04'
  And match driver.location == webUrlBase + '/page-04'
  And switchFrame('#frame01')
  When input('#eg01InputId', 'hello world')
  And click('#eg01SubmitId')
  Then match text('#eg01DivId') == 'hello world'

Examples:
    | config | dimensions |
    | { type: 'chrome' } | { left: 0, top: 0, width: 300, height: 800 } |
    | { type: 'chromedriver' } | { left: 50, top: 0, width: 250, height: 800 } |
    | { type: 'geckodriver' } | { left: 600, top: 0, width: 300, height: 800 } |
    | { type: 'safaridriver' } | { left: 1000, top: 0, width: 300, height: 800 } |
    # | { type: 'mswebdriver' } |
    # | { type: 'msedge' } |
    