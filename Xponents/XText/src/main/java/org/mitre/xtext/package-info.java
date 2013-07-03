/** <!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <meta http-equiv="content-type" content="text/html;charset=utf-8">
    <title></title>
  </head>
  <body>
    <h1> XText: Text Extraction from Multimedia Documents<br>
    </h1>
    This is an easier way to convert piles and piles of documents.&nbsp;
    From the command line, from your Java program.<br>
    <br>
    Usage
    <blockquote><font face="Courier New, Courier, monospace">xt =
        XText()</font><br>
      <font face="Courier New, Courier, monospace">// set various XText
        parameters to affect behavior.</font><br>
      <font face="Courier New, Courier, monospace">xt.save = True</font><br>
      <font face="Courier New, Courier, monospace">xt.archiveRoot =
        /some/path&nbsp; </font><br>
      <font face="Courier New, Courier, monospace">// if you wish to
        save content to disk <br>
        //<br>
        // Now affect files you want to convert. Add ignore types, add
        supported types<br>
        <br>
      </font><br>
      <font face="Courier New, Courier, monospace"><font face="Courier
          New, Courier, monospace">// Optionally xt.clear(), change
          settings then xt.setup() to initialize <br>
          // converters.&nbsp;&nbsp; xt.defaults() is called by default
          and includes most common file <br>
          // types.<br>
        </font>xt.ignoreFileType( 'xyz' ) // Ignores files *.xyz<br>
        xt.setup( )<br>
      </font><br>
      <font face="Courier New, Courier, monospace">xt.setConversionListener(&nbsp;


        you )&nbsp; </font><br>
      <font face="Courier New, Courier, monospace">// Where 'you' is
        some listener you setup to process a Converted Document.</font><br>
      <font face="Courier New, Courier, monospace">// That is, if you do
        not need or want to save to disk, you process the Document
        object and its payload in memory.</font><br>
      <br>
      <font face="Courier New, Courier, monospace">xt.extract_text( File
        )&nbsp; </font><br>
      <font face="Courier New, Courier, monospace">// a loop that
        iterates over File<br>
        //<br>
        // Now yer done.</font><br>
      <br>
    </blockquote>
    <font face="Garamond">The output is a stream of ConvertedDocument
      objects you process using an optional ConversionListener.<br>
      If you are saving files, they will appear at <b>XText().archiveRoot


      </b><br>
      Input files that are ZIPs will be unarchived at <b>XText().tempRoot</b>
      first, but immediately deleted when extraction finishes.&nbsp;
      Here it is important to use save flag + archiveRoot and/or have a
      listener set.&nbsp;&nbsp; Unpacking Zip files will lead to filling
      up your disk if they are not scrubbed.&nbsp; Since XText is
      unpacking them internally, it is also responsible for its own
      cleanup.&nbsp; Unzipped archives are deleted when extract_text( F
      ) routine finishes when F is a zip/tar/tar.gz, etc.<br>
      <br>
      TODO: nested Zip files.&nbsp; We do not unzip archives in archives
      for now.<br>
      <br>
      Main classes:<br>
    </font>
    <ul>
      <li><font face="Garamond"><b>XText</b> - the main program</font></li>
      <li><font face="Garamond"><b>ConvertedDocument</b> - the main
          output</font></li>
      <li><font face="Garamond"><b>iConvert</b> - interface for
          converting files </font></li>
      <li><font face="Garamond"><b>iFilter</b> - interface for filtering
          files</font></li>
      <li><font face="Garamond"><b>ConversionListener</b> - interface
          for any post-processor that will deal with ConvertedDocument</font></li>
    </ul>
    <font face="Garamond"><br>
      A "saved" ConvertedDocument will reside at your archiveRoot and
      will consist of the format:<br>
      <br>
    </font>
    <pre>----------------------<br>&lt;CONVERTED TEXT BODY, UTF-8 or ASCII encoded&gt;\n<br>\n<br>&lt;JSON metadata sheet, base64-encoded&gt;\n<br>----------------------</pre>
    <font face="Garamond">The intent of this format is for a number of
      reasons:<br>
    </font>
    <ul>
      <li><font face="Garamond"><b>Trac</b><b>k meta</b><b>data
            easily.&nbsp; </b>The format keeps the metadata about the
          conversion close to the original signal.</font></li>
      <li><font face="Garamond"><b>K</b><b>eep the textual content front
            and center</b><b>.</b><b> </b>The footer metadata follows
          to not disturb the natural order of the document.&nbsp; This
          is particularly important for natural language processing.&nbsp;
          The offsets of any tagging or annotation into the signal will
          not be altered by the presence of the metadata sheet that
          follows it.</font></li>
      <li><font face="Garamond"><b>Encode </b><b>properly.</b> Base64
          encoding protects the data from being disturbed by processing,
          while the JSON model is widely supported for storing key/value
          pairs</font></li>
      <li><font face="Garamond">Unix line-endings are the default, for
          consistency.<br>
        </font></li>
    </ul>
    <font face="Garamond"><br>
      Metadata properties tracked in the metadata header include:<br>
      <br>
    </font>
    <table border="1" cellpadding="2" cellspacing="2" width="100%">
      <tbody>
        <tr>
          <td valign="top"><font face="Garamond"><b>Field</b><b><br>
              </b></font></td>
          <td valign="top"><font face="Garamond"><b>Description</b><b><br>
              </b></font></td>
        </tr>
        <tr>
          <td valign="top"><font face="Garamond">title<br>
            </font></td>
          <td valign="top"><font face="Garamond">document title, per
              Tika.&nbsp; If null or untitled we may try to get a
              scrubbed first 100 chars.<br>
            </font></td>
        </tr>
        <tr>
          <td valign="top"><font face="Garamond">author<br>
            </font></td>
          <td valign="top"><font face="Garamond">document author<br>
            </font></td>
        </tr>
        <tr>
          <td valign="top"><font face="Garamond">subject<br>
            </font></td>
          <td valign="top"><font face="Garamond">subject keywords<br>
            </font></td>
        </tr>
        <tr>
          <td valign="top"><font face="Garamond">filepath<br>
            </font></td>
          <td valign="top"><font face="Garamond">file path to
              original.&nbsp; If unzipped archive this may be irrelevant
              or at least a relative path.&nbsp;&nbsp; TODO: format may
              be: file:///&lt;archive&gt;!&lt;file&gt;<br>
            </font></td>
        </tr>
        <tr>
          <td valign="top"><font face="Garamond">encoding<br>
            </font></td>
          <td valign="top"><font face="Garamond">native encoding<br>
            </font></td>
        </tr>
        <tr>
          <td valign="top"><font face="Garamond">pub_date<br>
            </font></td>
          <td valign="top"><font face="Garamond">best publication date
              for the document<br>
            </font></td>
        </tr>
        <tr>
          <td valign="top"><font face="Garamond">encrypted<br>
            </font></td>
          <td valign="top"><font face="Garamond">Yes | No<br>
            </font></td>
        </tr>
        <tr>
          <td valign="top"><font face="Garamond">creator_tool<br>
            </font></td>
          <td valign="top"><font face="Garamond">authoring tool used to
              create the document<br>
            </font></td>
        </tr>
        <tr>
          <td valign="top"><font face="Garamond">converter<br>
            </font></td>
          <td valign="top"><font face="Garamond">conversion class<br>
            </font></td>
        </tr>
        <tr>
          <td valign="top"><font face="Garamond">conversion_date<br>
            </font></td>
          <td valign="top"><font face="Garamond">date of conversion<br>
            </font></td>
        </tr>
        <tr>
          <td valign="top"><font face="Garamond">filtered<br>
            </font></td>
          <td valign="top"><font face="Garamond">True |
              False.&nbsp;&nbsp; If the content was scrubbed beyond rote
              file conversion.&nbsp; Web HTML articles are likely the
              only case now.&nbsp; HTML content that is filtered is
              converted, boiler-plate junk removed, and empty lines are
              reduced.<br>
              <br>
            </font></td>
        </tr>
      </tbody>
    </table>
    <font face="Garamond"><br>
    </font>
    <h1><font face="Garamond">Supported Formats</font></h1>
    <font face="Garamond"><br>
    </font>
    <table border="1" cellpadding="2" cellspacing="2" width="100%">
      <tbody>
        <tr>
          <td valign="top"><font face="Garamond">File Extension<br>
            </font></td>
          <td valign="top"><font face="Garamond">Converter Class<br>
            </font></td>
        </tr>
        <tr>
          <td valign="top"><font face="Garamond">.doc<br>
            </font></td>
          <td valign="top"><font face="Garamond">MSDocConverter<br>
            </font></td>
        </tr>
        <tr>
          <td valign="top"><font face="Garamond">.html, .htm<br>
            </font></td>
          <td valign="top"><font face="Garamond">TikaHTMLConverter<br>
              XText.scrub_article&nbsp; = true | false to affect HTML
              scrubbing.&nbsp; Scrubbing is good for pure web content,
              but if you have HTML that originated from within your
              IntraNet, scrubbing may remove valid content.<br>
            </font></td>
        </tr>
        <tr>
          <td valign="top"><font face="Garamond">.pdf<br>
            </font></td>
          <td valign="top"><font face="Garamond">PDFConverter makes use
              of PDFBox. This may be ported to the Tika parser.<br>
              To use <br>
            </font></td>
        </tr>
        <tr>
          <td valign="top"><font face="Garamond">MS Office files, RTF<br>
            </font></td>
          <td valign="top"><font face="Garamond">DefaultConverter a
              wrapper around Tika.<br>
            </font></td>
        </tr>
        <tr>
          <td valign="top"><font face="Garamond">*.txt<br>
            </font></td>
          <td valign="top"><font face="Garamond">Plain text files are a
              bit difficult -- the main issue here is detecting encoding
              properly.&nbsp; XText tries to detect ASCII, UTF-8 or
              other encodings as best as it can.&nbsp; ASCII/UTF-8 files
              will not be saved -- they will be read in and emitted as
              trivial ConvertedDocuments.&nbsp;&nbsp; But are never
              cached or saved to output archive set by archiveRoot.<br>
              <br>
              Short texts with low confidence of encoding will also not
              be saved/archived.&nbsp; They will be emitted though.<br>
              <br>
              Texts longer than 1KB with a encodings other than ASCII or
              UTF-8 will be transcoded (to UTF-8) and converted.<br>
              <br>
            </font></td>
        </tr>
        <tr>
          <td valign="top"><font face="Garamond"><br>
            </font></td>
          <td valign="top"><font face="Garamond"><br>
            </font></td>
        </tr>
      </tbody>
    </table>
    <font face="Garamond"><br>
    </font>
    <h1><br>
    </h1>
  </body>
</html>
*/
package org.mitre.xtext;
